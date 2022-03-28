package com.etherblood.luna.network.api;

import java.util.UUID;

public interface GameApi {

    GameState join(UUID gameId);

    void leave(UUID gameId);

    UUID startGame(GameSettings settings);
}
