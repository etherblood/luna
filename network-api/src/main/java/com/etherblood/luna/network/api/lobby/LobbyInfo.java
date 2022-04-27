package com.etherblood.luna.network.api.lobby;

import java.util.List;
import java.util.UUID;

public record LobbyInfo(
        UUID gameId,
        long startEpochMillis,
        String gameTemplate,
        List<Player> players
) {
}
