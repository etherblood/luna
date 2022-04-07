package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.damage.Team;
import com.etherblood.luna.engine.movement.Obstaclebox;

public class LocalMain {

    public static void main(String... args) {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        new ApplicationClient(localProxy()).start();
    }

    static GameProxy localProxy() {
        JwtAuthenticationUser user = getTestJwtUser(1);
        GameEngine game = GameRules.getDefault().createGame();

        EntityData data = game.getData();
        int player = data.createEntity();
        game.applyTemplate(player, "amara");
        data.set(player, new PlayerId(user.id));
        data.set(player, new ActorName(user.login));
        data.set(player, new Position(0, 0));
        data.set(player, Team.PLAYERS);

        int obstacle = data.createEntity();
        data.set(obstacle, new Obstaclebox(new Rectangle(1000, -1000, 2000, 2000)));
        data.set(obstacle, new Position(new Vector2(0, 0)));

        return new LocalGameProxy(game, user);
    }

    static JwtAuthenticationUser getTestJwtUser(long playerId) {
        return new JwtAuthenticationUser(playerId, "player " + playerId);
    }
}
