package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.PlayerName;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Speed;
import java.io.IOException;

public class LocalMain {

    public static void main(String... args) throws IOException {
        new ApplicationClient(localProxy()).start();
    }

    static GameProxy localProxy() {
        JwtAuthenticationUser user = getTestJwtUser(1);
        GameEngine game = GameRules.getDefault().createGame();

        EntityData data = game.getData();
        int player = data.createEntity();
        data.set(player, new PlayerId(user.id));
        data.set(player, new PlayerName(user.login));
        data.set(player, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(player, new Position(0, 0));
        data.set(player, new Speed(0, 0));
        data.set(player, new ActorState(ActorAction.IDLE, Direction.NONE, 0));

        return new LocalGameProxy(game, user, 60);
    }

    static JwtAuthenticationUser getTestJwtUser(long playerId) {
        return new JwtAuthenticationUser(playerId, "player " + playerId);
    }
}
