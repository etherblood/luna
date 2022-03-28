package com.etherblood.luna.application.client;

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

public class Main {

    public static void main(String... args) {
        GameEngine game = GameRules.getDefault().createGame();

        EntityData data = game.getData();
        int player = data.createEntity();
        int character = data.createEntity();
        data.set(character, new OwnedBy(player));
        data.set(character, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character, new Position(0, 0));
        data.set(character, Direction.RIGHT);
        data.set(character, new Speed(0, 0));
        data.set(character, new ActorState(ActorAction.IDLE, Direction.RIGHT, 0));

        new ApplicationClient(new GameProxy(game, player)).start();
    }
}
