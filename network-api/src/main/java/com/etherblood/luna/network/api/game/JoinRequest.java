package com.etherblood.luna.network.api.game;

import java.util.UUID;

public record JoinRequest(
        UUID gameId,
        String actorTemplate
) {
}
