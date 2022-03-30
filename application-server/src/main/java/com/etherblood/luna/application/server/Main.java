package com.etherblood.luna.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.server.ServerGameModule;
import com.etherblood.luna.network.server.timestamp.ServerTimestampModule;
import java.io.IOException;
import java.util.Date;

public class Main {
    public static void main(String... args) throws IOException {
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        System.err.println("WARNING: Using jwt service without validation.");
        JwtService jwtService = new NoValidateJwtService();

        System.out.println("Unsafe access warnings are a known issue, see: https://github.com/EsotericSoftware/kryonet/issues/154");

        GameEngine game = GameRules.getDefault().createGame();

        Server server = new Server(10_0000, 10_000);
        JwtServerModule jwtModule = new JwtServerModule(jwtService, server::getConnections);
        ServerTimestampModule timestampModule = new ServerTimestampModule();
        ServerGameModule gameModule = new ServerGameModule(game);
        ToolsServer toolsServer = new ToolsServer(server, jwtModule, timestampModule, gameModule);

        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        int fps = 60;
        GameLoop loop = new GameLoop(fps, () -> {
            gameModule.tick();
        });
        loop.run();
    }
}