package com.etherblood.luna.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.destrostudios.gametools.network.server.modules.game.LobbyServerModule;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.api.lobby.LobbyInfo;
import com.etherblood.luna.network.server.GameServerModule;
import com.etherblood.luna.network.server.chat.ServerChatModule;
import com.etherblood.luna.network.server.timestamp.TimestampServerModule;
import java.io.IOException;
import java.util.Date;

public class Main {
    public static void main(String... args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        System.err.println("WARNING: Using jwt service without validation.");
        JwtService jwtService = new NoValidateJwtService();

        Server server = new Server(10_0000, 10_000);
        JwtServerModule jwtModule = new JwtServerModule(jwtService, server::getConnections);
        TimestampServerModule timestampModule = new TimestampServerModule();
        GameServerModule gameModule = new GameServerModule(jwtModule);
        LobbyServerModule<Object> lobbyModule = new LobbyServerModule<>(kryo -> {
            kryo.register(LobbyInfo.class, new RecordSerializer<>());
        }, server::getConnections);
        ServerChatModule chatModule = new ServerChatModule(jwtModule, server::getConnections);
        ToolsServer toolsServer = new ToolsServer(server, jwtModule, timestampModule, gameModule, lobbyModule, chatModule);

        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        int fps = 120;
        GameLoop loop = new GameLoop(fps, () -> {
            gameModule.update();
        });
        loop.run();
    }
}
