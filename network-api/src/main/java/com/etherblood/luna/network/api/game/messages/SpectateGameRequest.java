package com.etherblood.luna.network.api.game.messages;

import java.util.UUID;

public record SpectateGameRequest(
        UUID gameId
) {
}
