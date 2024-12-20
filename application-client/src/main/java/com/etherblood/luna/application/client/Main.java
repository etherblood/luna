package com.etherblood.luna.application.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.application.client.game.GameProxy;
import com.etherblood.luna.application.client.game.RemoteGameProxy;
import com.etherblood.luna.application.client.gui.GuiFactory;
import com.etherblood.luna.application.client.gui.GuiManager;
import com.etherblood.luna.application.client.lobby.ChatSystem;
import com.etherblood.luna.application.client.lobby.CommandService;
import com.etherblood.luna.application.client.lobby.LobbySystem;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import com.etherblood.luna.network.client.lobby.LunaLobbyClientModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

public class Main {

    public static void main(String... args) throws IOException {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("First argument must be a jwt.");
            }
            startApp("destrostudios.com", args[0], false);
        } catch (Throwable t) {
            showErrorDialog(t);
            throw t;
        }
    }

    static void showErrorDialog(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        JOptionPane.showMessageDialog(null, sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static ApplicationClient startApp(String host, String jwt, boolean debug) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        if (debug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
//            Log.DEBUG(); // very spammy
        }
        Log.info(new Date().toString());// time reference for kryo logs
        ToolsClient toolsClient = createToolsClient();
        ApplicationClient app = new ApplicationClient(Main.remoteProxy(toolsClient, host, jwt)) {
            @Override
            protected void init() {
                super.init();
                CommandService commandService = new CommandService(
                        this::stop, toolsClient.getModule(GameClientModule.class));
                addSystem(new ChatSystem(
                        toolsClient.getModule(ClientChatModule.class),
                        commandService,
                        getSystem(GuiManager.class),
                        getSystem(GuiFactory.class)));
                addSystem(new LobbySystem(
                        toolsClient.getModule(LobbyClientModule.class),
                        toolsClient.getModule(GameClientModule.class),
                        toolsClient.getModule(TimestampClientModule.class),
                        getSystem(GuiManager.class),
                        getSystem(GuiFactory.class)));
            }
        };
        app.getConfig().setEnableValidationLayer(debug);
        app.start();
        return app;
    }

    static ToolsClient createToolsClient() {
        Client client = new Client(1_000_000, 1_000_000);
        JwtClientModule jwtModule = new JwtClientModule(client);
        TimestampClientModule timestampModule = new TimestampClientModule(client, 40, 250);
        LunaLobbyClientModule lobbyModule = new LunaLobbyClientModule(client);
        GameClientModule gameModule = new GameClientModule(timestampModule, client);
        ClientChatModule chatModule = new ClientChatModule(client);
        return new ToolsClient(client, gameModule, jwtModule, timestampModule, lobbyModule, chatModule);
    }

    static GameProxy remoteProxy(ToolsClient toolsClient, String host, String jwt) throws IOException {

        Client client = toolsClient.getKryoClient();
        client.start();
        client.connect(10_000, host, NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        JwtClientModule jwtModule = toolsClient.getModule(JwtClientModule.class);
        GameClientModule clientModule = toolsClient.getModule(GameClientModule.class);
        TimestampClientModule timestampModule = toolsClient.getModule(TimestampClientModule.class);

        jwtModule.login(jwt);
        clientModule.spectate(GameModule.LOBBY_GAME_ID);
        clientModule.enter("amara");

        return new RemoteGameProxy(
                timestampModule,
                clientModule,
                new NoValidateJwtService().decode(jwt).user);
    }

    public static String getTestJwt(long playerId) {
        Map<String, ?> user = Map.of("id", playerId, "login", "player " + playerId);
        return JWT.create()
                .withIssuedAt(new Date())
                .withClaim("user", user)
                .sign(Algorithm.none());
    }
}
