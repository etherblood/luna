package com.etherblood.lunia.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.destrostudios.gametools.network.server.ToolsServer;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameLoop;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
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
        EntityData data = game.getData();

        int character1 = data.createEntity();
        data.set(character1, new OwnedBy(1));
        data.set(character1, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character1, new Position(0, 0));
        data.set(character1, new Speed(0, 0));
        data.set(character1, new ActorState(ActorAction.IDLE, Direction.NONE, 0));
        data.set(character1, Direction.RIGHT);


        int character2 = data.createEntity();
        data.set(character2, new OwnedBy(2));
        data.set(character2, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character2, new Position(1000, 0));
        data.set(character2, new Speed(0, 0));
        data.set(character2, new ActorState(ActorAction.IDLE, Direction.NONE, 0));
        data.set(character2, Direction.RIGHT);

        Server server = new Server(10_0000, 10_000);
        ServerTimestampModule timestampModule = new ServerTimestampModule();
        ServerGameModule gameModule = new ServerGameModule(game);
        ToolsServer toolsServer = new ToolsServer(server, timestampModule, gameModule);
        
        server.start();
        server.bind(NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        int fps = 60;
        GameLoop loop = new GameLoop(fps, () -> {
            gameModule.tick();
        });
        loop.run();
    }
}
