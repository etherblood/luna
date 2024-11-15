package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.data.ActionType;

public record ActorInput(
        Direction direction,
        ActionType action
) {
}
