package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.data.ActionType;

public record PlayerInput(
        long player,
        Direction direction,
        ActionType action
) {
}
