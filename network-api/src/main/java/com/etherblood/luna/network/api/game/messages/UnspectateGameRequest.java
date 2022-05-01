package com.etherblood.luna.network.api.game.messages;

import java.util.Objects;
import java.util.UUID;

public record UnspectateGameRequest(
        UUID gameId
) {
    public UnspectateGameRequest {
        Objects.requireNonNull(gameId);
    }
}
