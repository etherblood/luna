package com.etherblood.luna.network.api.game.messages;

import com.etherblood.luna.engine.GameEngine;
import java.util.Objects;
import java.util.UUID;

public record SpectateGameResponse(
        UUID spectateId,
        GameEngine game
) {
    public SpectateGameResponse {
        Objects.requireNonNull(spectateId);
        Objects.requireNonNull(game);
    }
}
