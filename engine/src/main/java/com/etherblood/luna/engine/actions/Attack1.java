package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public class Attack1 extends Action {
    private static final long DAMAGE_FRAME = 100;
    private static final long INTERRUPT_RESIST_FRAMES = 160;
    private static final long DURATION_FRAMES = 280;

    private final long elapsedFrames;

    public Attack1(long elapsedFrames) {
        this.elapsedFrames = elapsedFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.ATTACK1;
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
