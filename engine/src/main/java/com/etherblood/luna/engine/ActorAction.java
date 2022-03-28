package com.etherblood.luna.engine;

public enum ActorAction {
    IDLE,
    DASH;

    public boolean isInterruptible() {
        return switch (this) {
            case IDLE, DASH -> true;
            default -> false;
        };
    }

    public boolean isTurnable() {
        return switch (this) {
            case IDLE -> true;
            default -> false;
        };
    }
}
