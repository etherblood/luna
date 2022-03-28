package com.etherblood.lunia.application.server;

import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.api.GameSettings;
import java.util.Map;
import java.util.UUID;

public record GameData(
        UUID id,
        GameEngine engine,
        GameSettings settings,
        Map<Long, PlayerInput[]> framePlayerInputs
) {
}
