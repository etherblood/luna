package com.etherblood.luna.network.api.lobby;

import java.util.UUID;

public record LobbyInfo(
        UUID lobbyId,
        String lobbyName,
        long[] players,
        UUID runningGameId
) {
}
