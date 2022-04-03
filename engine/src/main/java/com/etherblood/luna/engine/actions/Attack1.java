package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public class Attack1 extends Action {
    private static final long DAMAGE_FRAME = 100;
    private static final long INTERRUPT_RESIST_FRAMES = 160;
    private static final long DURATION_FRAMES = 280;

    @Override
    public ActionKey getKey() {
        return ActionKey.ATTACK1;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > DURATION_FRAMES;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) < INTERRUPT_RESIST_FRAMES ? 2 : 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        if (getElapsedFrames(game, actor) == DAMAGE_FRAME) {
            // damage magic
        }
    }
}
