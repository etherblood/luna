package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionKey;

public record ActorInput(
        Direction direction,
        ActionKey action
) {
}