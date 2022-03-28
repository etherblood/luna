package com.etherblood.luna.engine;

public record ActorState(
        ActorAction action,
        Direction direction,
        long startTick
) {
}
