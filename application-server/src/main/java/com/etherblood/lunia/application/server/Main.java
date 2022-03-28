package com.etherblood.lunia.application.server;

import com.destrostudios.authtoken.JwtService;
import com.destrostudios.authtoken.NoValidateJwtService;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.Player;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
import java.util.Date;

public class Main {
    public static void main(String... args) {
        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        System.err.println("WARNING: Using jwt service without validation.");
        JwtService jwtService = new NoValidateJwtService();

        System.out.println("Unsafe access warnings are a known issue, see: https://github.com/EsotericSoftware/kryonet/issues/154");

        Server kryoServer = new Server(10_000_000, 10_000_000);

        GameEngine game = GameRules.getDefault().createGame();
        EntityData data = game.getData();

        int player1 = data.createEntity();
        data.set(player1, new Player(1, "player1"));
        int character1 = data.createEntity();
        data.set(character1, new OwnedBy(player1));
        data.set(character1, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character1, new Position(0, 0));
        data.set(character1, new Speed(0, 0));
        data.set(character1, new ActorState(ActorAction.IDLE, Direction.RIGHT, 0));


        int player2 = data.createEntity();
        data.set(player2, new Player(2, "player2"));
        int character2 = data.createEntity();
        data.set(character2, new OwnedBy(player2));
        data.set(character2, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character2, new Position(10000, 0));
        data.set(character2, new Speed(0, 0));
        data.set(character2, new ActorState(ActorAction.IDLE, Direction.LEFT, 0));

        Server server = new Server();
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {

            }

            @Override
            public void disconnected(Connection connection) {

            }

            @Override
            public void received(Connection connection, Object object) {

            }
        });
    }
}
