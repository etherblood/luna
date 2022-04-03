package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public class Idle extends Action {

    private final long elapsedFrames;

    public Idle(long elapsedFrames) {
        this.elapsedFrames = elapsedFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.IDLE;
    }

    @Override
    public boolean hasEnded() {
        return false;
    }

    @Override
    protected int interruptResistance() {
        return 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        // do nothing
    }

    @Override
    public boolean isTurnable() {
        return true;
    }
}
