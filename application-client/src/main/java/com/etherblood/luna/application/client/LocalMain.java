package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActorName;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Team;

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

        game.applyTemplate(data.createEntity(), "test_room");

        return new LocalGameProxy(game, user);
    }

    static JwtAuthenticationUser getTestJwtUser(long playerId) {
        return new JwtAuthenticationUser(playerId, "player " + playerId);
    }
}
