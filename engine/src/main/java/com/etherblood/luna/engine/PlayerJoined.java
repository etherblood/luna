package com.etherblood.luna.engine;

public record PlayerJoined(
        long playerId,
        String playerName,
        String actorTemplate,
        boolean enter
) {
}
