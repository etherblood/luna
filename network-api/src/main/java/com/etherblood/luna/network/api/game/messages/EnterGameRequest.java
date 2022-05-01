package com.etherblood.luna.network.api.game.messages;

import java.util.Objects;
import java.util.UUID;

public record EnterGameRequest(
        UUID gameId,
        String actorTemplate
) {
    public EnterGameRequest {
        Objects.requireNonNull(gameId);
        Objects.requireNonNull(actorTemplate);
    }
}
