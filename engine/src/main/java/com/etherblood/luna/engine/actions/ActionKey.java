package com.etherblood.luna.engine.actions;

public enum ActionKey {
    IDLE,
    WALK,
    DASH,
    ATTACK1,
    ATTACK2;

    int interruptStrength() {
        return switch (this) {
            case IDLE -> 0;
            case WALK -> 0;
            case DASH -> 2;
            case ATTACK1 -> 1;
            case ATTACK2 -> 1;
        };
    }
}
