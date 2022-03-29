package com.etherblood.luna.application.client;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerInput;

public interface GameProxy {

    GameEngine getEngineSnapshot();

    void requestInput(PlayerInput input);

    int getPlayer();

    void update();
}
