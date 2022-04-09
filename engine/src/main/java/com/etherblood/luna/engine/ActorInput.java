package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.data.ActionKey;

public record ActorInput(
        Direction direction,
        ActionKey action
) {
}
