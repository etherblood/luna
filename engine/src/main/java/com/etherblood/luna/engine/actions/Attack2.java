package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public class Attack2 extends Action {
    private static final long DAMAGE_FRAME = 64;
    private static final long INTERRUPT_RESIST_FRAMES = 100;
    private static final long DURATION_FRAMES = 160;

    private final long elapsedFrames;

    public Attack2(long elapsedFrames) {
        this.elapsedFrames = elapsedFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.ATTACK2;
    }

    @Override
    public boolean hasEnded() {
        return elapsedFrames > DURATION_FRAMES;
    }

    @Override
    protected int interruptResistance() {
        return elapsedFrames < INTERRUPT_RESIST_FRAMES ? 2 : 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        if (elapsedFrames == DAMAGE_FRAME) {
            // damage magic
        }
    }
}
