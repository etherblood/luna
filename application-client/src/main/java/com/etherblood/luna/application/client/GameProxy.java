package com.etherblood.luna.application.client;

import com.destrostudios.authtoken.JwtAuthenticationUser;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerInput;

public interface GameProxy {

    GameEngine getEngineSnapshot();

    JwtAuthenticationUser getPlayer();

    void update(PlayerInput requestedInput);

    long getLatency();
}
