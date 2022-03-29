package com.etherblood.luna.application.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
import com.etherblood.luna.network.api.NetworkModule;
import com.etherblood.luna.network.api.NetworkUtil;
import com.etherblood.luna.network.client.ClientGameModule;
import com.etherblood.luna.network.client.timestamp.ClientTimestampModule;
import java.io.IOException;
import java.util.Date;

public class Main {

    public static void main(String... args) throws IOException {
        new ApplicationClient(localProxy()).start();
    }

    static GameProxy remoteProxy(int player) throws IOException {
//        Log.DEBUG();
        Log.info(new Date().toString());// time reference for kryo logs
        Client client = new Client(10_000, 10_000);

        ClientTimestampModule timestampModule = new ClientTimestampModule(client, 1000, 500);
        NetworkModule.addModule(client, timestampModule);

        ClientGameModule gameModule = new ClientGameModule(client);
        NetworkModule.addModule(client, gameModule);

        client.start();
        client.connect(10_000, "localhost", NetworkUtil.TCP_PORT, NetworkUtil.UDP_PORT);

        return new RemoteGameProxy(timestampModule, gameModule, player);

    }

    static GameProxy localProxy() {
        GameEngine game = GameRules.getDefault().createGame();

        EntityData data = game.getData();
        int player = data.createEntity();
        int character = data.createEntity();
        data.set(character, new OwnedBy(player));
        data.set(character, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character, new Position(0, 0));
        data.set(character, new Speed(0, 0));
        data.set(character, new ActorState(ActorAction.IDLE, Direction.RIGHT, 0));
        data.set(character, Direction.RIGHT);

        return new LocalGameProxy(game, player, 60);
    }
}
