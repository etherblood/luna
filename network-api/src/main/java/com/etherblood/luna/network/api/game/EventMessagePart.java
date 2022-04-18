package com.etherblood.luna.network.api.game;

import com.etherblood.luna.engine.GameEvent;

public record EventMessagePart(
        long frame,
        GameEvent event
) {
}
