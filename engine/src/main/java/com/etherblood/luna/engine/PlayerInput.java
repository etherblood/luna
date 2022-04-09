package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.data.ActionKey;

public record PlayerInput(
        long player,
        Direction direction,
        ActionKey action
) {
}
