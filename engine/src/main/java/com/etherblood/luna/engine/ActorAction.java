package com.etherblood.luna.engine;

public enum ActorAction {
    IDLE,
    DASH;

    public boolean interrupts(ActorAction previous) {
        return this != previous && interrupts() >= previous.isInterruptedBy();
    }

    private int isInterruptedBy() {
        return switch (this) {
            case IDLE -> 0;
            case DASH -> 1;
            default -> 0;
        };
    }

    private int interrupts() {
        return switch (this) {
            case IDLE -> 0;
            case DASH -> 2;
            default -> 0;
        };
    }

    public boolean isTurnable() {
        return switch (this) {
            case IDLE -> true;
            default -> false;
        };
    }
}
