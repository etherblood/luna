package com.etherblood.luna.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.destrostudios.gametools.network.server.modules.jwt.JwtServerModule;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.movement.Obstaclebox;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.server.ServerGameModule;
import com.etherblood.luna.network.server.timestamp.ServerTimestampModule;
import java.io.IOException;
import java.util.Date;

public class Main {
    public static void main(String... args) throws IOException {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        System.err.println("WARNING: Using jwt service without validation.");
        JwtService jwtService = new NoValidateJwtService();

        GameEngine game = GameRules.getDefault().createGame();

        // TODO: this does not belong here
        EntityData data = game.getData();
        Obstaclebox obstaclebox = new Obstaclebox(new Rectangle(-10_000, -10_000, 20_000, 20_000));
        int leftObstacle = data.createEntity();
        data.set(leftObstacle, obstaclebox);
        data.set(leftObstacle, new Position(new Vector2(-15_000, 0)));
        int rightObstacle = data.createEntity();
        data.set(rightObstacle, obstaclebox);
        data.set(rightObstacle, new Position(new Vector2(15_000, 0)));
        int bottomObstacle = data.createEntity();
        data.set(bottomObstacle, obstaclebox);
        data.set(bottomObstacle, new Position(new Vector2(0, -15_000)));
        int topObstacle = data.createEntity();
        data.set(topObstacle, obstaclebox);
        data.set(topObstacle, new Position(new Vector2(0, 15_000)));

        Server server = new Server(10_0000, 10_000);
        JwtServerModule jwtModule = new JwtServerModule(jwtService, server::getConnections);
        ServerTimestampModule timestampModule = new ServerTimestampModule();
        ServerGameModule gameModule = new ServerGameModule(game);
        ToolsServer toolsServer = new ToolsServer(server, jwtModule, timestampModule, gameModule);

        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        int fps = game.getRules().getFramesPerSecond();
        GameLoop loop = new GameLoop(fps, () -> {
            gameModule.update();
        });
        loop.run();
    }
}
