package com.etherblood.luna.application.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.client.ToolsClient;
import com.destrostudios.gametools.network.client.modules.game.LobbyClientModule;
import com.destrostudios.gametools.network.client.modules.jwt.JwtClientModule;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.api.game.GameModule;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.client.GameClientModule;
import com.etherblood.luna.network.client.chat.ClientChatModule;
import com.etherblood.luna.network.client.timestamp.TimestampClientModule;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String... args) throws IOException {
        try {
            startApp(args);
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

    private static void startApp(String[] args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        if (args.length == 0) {
            throw new IllegalArgumentException("First argument must be a jwt.");
        }
        ToolsClient toolsClient = createToolsClient();
        ApplicationClient app = new ApplicationClient(Main.remoteProxy(toolsClient, "destrostudios.com", args[0])) {
            @Override
            protected void init() {
                super.init();
                CommandService commandService = new CommandService(
                        toolsClient.getModule(JwtClientModule.class),
                        toolsClient.getModule(TimestampClientModule.class),
                        toolsClient.getModule(GameClientModule.class),
                        toolsClient.getModule(LobbyClientModule.class));
                addSystem(new ChatSystem(toolsClient.getModule(ClientChatModule.class), commandService));
            }
        };
        app.start();
    }

    static ToolsClient createToolsClient() {
        Client client = new Client(10_000, 10_000);
        JwtClientModule jwtModule = new JwtClientModule(client);
        TimestampClientModule timestampModule = new TimestampClientModule(client, 40, 250);
        GameClientModule gameModule = new GameClientModule(client);
        LobbyClientModule<Object> lobbyModule = new LobbyClientModule<>(kryo -> {
            kryo.register(LobbyInfo.class, new RecordSerializer<>());
        }, client);
        ClientChatModule chatModule = new ClientChatModule(client);
        return new ToolsClient(client, jwtModule, timestampModule, gameModule, lobbyModule, chatModule);
    }

    static GameProxy remoteProxy(ToolsClient toolsClient, String host, String jwt) throws IOException {
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs

        Client client = toolsClient.getKryoClient();
        client.start();
        client.connect(10_000, host, NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        toolsClient.getModule(JwtClientModule.class).login(jwt);
        toolsClient.getModule(GameClientModule.class).spectate(GameModule.LOBBY_GAME_ID);
        return new RemoteGameProxy(
                toolsClient.getModule(TimestampClientModule.class),
                toolsClient.getModule(GameClientModule.class),
                new NoValidateJwtService().decode(jwt).user);
    }

    static String getTestJwt(long playerId) {
        Map<String, ?> user = Map.of("id", playerId, "login", "player " + playerId);
        return JWT.create()
                .withIssuedAt(new Date())
                .withClaim("user", user)
                .sign(Algorithm.none());
    }
}
