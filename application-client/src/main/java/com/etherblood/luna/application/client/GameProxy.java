package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerInput;

public interface GameProxy {

    GameEngine getEngineSnapshot();

    void requestInput(PlayerInput input);

    JwtAuthenticationUser getPlayer();

    void update();

    long getLatency();
}
