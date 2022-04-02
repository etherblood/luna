package com.etherblood.luna.engine;

public record ActorState(
        ActorAction action,
        long startFrame
) {
}
