package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionKey;

public record ActorState(
        ActionKey action,
        long startFrame
) {
}
