package com.etherblood.luna.engine;

public record PlayerInput(
        int player,
        Direction direction,
        ActorAction action
) {
}
