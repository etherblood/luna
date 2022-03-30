package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Movebox;
import com.etherblood.luna.engine.OwnedBy;
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
        int character = data.createEntity();
        data.set(character, new OwnedBy(user.id));
        data.set(character, new Movebox(new Rectangle(-250, -250, 500, 500)));
        data.set(character, new Position(0, 0));
        data.set(character, new Speed(0, 0));
        data.set(character, new ActorState(ActorAction.IDLE, Direction.RIGHT, 0));
        data.set(character, Direction.RIGHT);

        return new LocalGameProxy(game, user, 60);
    }

    static JwtAuthenticationUser getTestJwtUser(long playerId) {
        return new JwtAuthenticationUser(playerId, "player " + playerId);
//        Map<String, ?> user = Map.of("id", playerId, "login", "player " + playerId);
//        return JWT.create()
//                .withIssuedAt(new Date())
//                .withClaim("user", user)
//                .sign(Algorithm.none());
    }
}
