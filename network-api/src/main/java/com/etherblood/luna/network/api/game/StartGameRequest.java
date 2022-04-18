package com.etherblood.luna.network.api.game;

import java.util.UUID;

public record StartGameRequest(
        UUID gameId,
        String gameRules,
        String gameTemplate
) {
}
