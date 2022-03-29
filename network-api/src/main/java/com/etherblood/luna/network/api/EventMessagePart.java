package com.etherblood.luna.network.api;

import com.etherblood.luna.engine.GameEvent;

public record EventMessagePart(
        long frame,
        GameEvent event
) {
}
