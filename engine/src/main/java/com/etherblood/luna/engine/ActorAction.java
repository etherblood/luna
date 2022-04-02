package com.etherblood.luna.engine;

public enum ActorAction {
    IDLE,
    WALK,
    DASH,
    ATTACK1,
    ATTACK2,
    DEATH;

    public boolean interrupts(ActorAction previous) {
        return this != previous && interrupts() >= previous.isInterruptedBy();
    }

    private int isInterruptedBy() {
        return switch (this) {
            case IDLE -> 0;
            case WALK -> 0;
            case DASH -> 1;
            case ATTACK1 -> 2;
            case ATTACK2 -> 2;
            case DEATH -> Integer.MAX_VALUE;
        };
    }

    private int interrupts() {
        return switch (this) {
            case IDLE -> 0;
            case WALK -> 0;
            case DASH -> 2;
            case ATTACK1 -> 1;
            case ATTACK2 -> 1;
            case DEATH -> Integer.MAX_VALUE - 1;
        };
    }

    public boolean isTurnable() {
        return switch (this) {
            case IDLE -> true;
            case WALK -> true;
            default -> false;
        };
    }
}
