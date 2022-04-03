package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionKey;

public record PlayerInput(
        long player,
        Direction direction,
        ActionKey action
) {
}
