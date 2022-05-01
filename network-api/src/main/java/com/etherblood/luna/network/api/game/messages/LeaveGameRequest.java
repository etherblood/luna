package com.etherblood.luna.network.api.game.messages;

import java.util.Objects;
import java.util.UUID;

public record LeaveGameRequest(
        UUID gameId
) {
    public LeaveGameRequest {
        Objects.requireNonNull(gameId);
    }
}
