package com.etherblood.luna.network.api.game.messages;

import java.util.UUID;

public record StartGameRequest(
        UUID gameId,
        String gameRules,
        String gameTemplate
) {
}
