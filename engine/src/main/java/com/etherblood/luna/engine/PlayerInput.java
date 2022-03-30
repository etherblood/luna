package com.etherblood.luna.engine;

public record PlayerInput(
        long player,
        Direction direction,
        ActorAction action
) {
}
