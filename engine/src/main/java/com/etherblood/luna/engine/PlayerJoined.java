package com.etherblood.luna.engine;

public record PlayerJoined(
        long playerId,
        String playerName,
        boolean enter
) {
}
