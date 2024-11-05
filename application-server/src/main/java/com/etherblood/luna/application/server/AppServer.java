package com.etherblood.luna.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.api.StatsSerialization;
import com.etherblood.luna.network.server.GameServerModule;
import com.etherblood.luna.network.server.chat.ServerChatModule;
import com.etherblood.luna.network.server.lobby.LunaLobbyServerModule;
import com.etherblood.luna.network.server.timestamp.TimestampServerModule;

import java.io.IOException;

public class AppServer {
    private final int fps = 60;
    private final Server server;
    private final GameServerModule gameModule;
    private final StatsSerialization serialization;
    private GameLoop loop;
    private long lastSerializationLog = -1;
    private final long serializationLogIntervalMillis = 60_000;

    public AppServer(JwtService jwtService) {
        serialization = new StatsSerialization();
        server = new Server(1_000_000, 1_000_000, serialization);
        JwtServerModule jwtModule = new JwtServerModule(jwtService, server::getConnections);
        TimestampServerModule timestampModule = new TimestampServerModule();
        LunaLobbyServerModule lobbyModule = new LunaLobbyServerModule(server::getConnections);
        gameModule = new GameServerModule(jwtModule, lobbyModule);
        ServerChatModule chatModule = new ServerChatModule(jwtModule, server::getConnections);
        ToolsServer toolsServer = new ToolsServer(server, gameModule, jwtModule, timestampModule, lobbyModule, chatModule);

    }

    public void start() throws IOException {
        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        new Thread(this::run).start();
    }

    public void run() {
        loop = new GameLoop(fps, () -> {
            gameModule.update();
            long current = System.currentTimeMillis() / serializationLogIntervalMillis;
            if (current > lastSerializationLog) {
                System.out.println("reads: " + serialization.reads);
                System.out.println("writes: " + serialization.writes);
                lastSerializationLog = current;
            }
        });
        loop.run();
    }

    public void stop() {
        loop.stop();
        server.stop();
    }
}
