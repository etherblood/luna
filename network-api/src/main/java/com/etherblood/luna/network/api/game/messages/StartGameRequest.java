package com.etherblood.luna.network.api.game.messages;

import java.util.Objects;
import java.util.UUID;

public record StartGameRequest(
        UUID gameId,
        String gameRules,
        String gameTemplate
) {
    public StartGameRequest {
        Objects.requireNonNull(gameId);
        Objects.requireNonNull(gameRules);
        Objects.requireNonNull(gameTemplate);
    }
}
