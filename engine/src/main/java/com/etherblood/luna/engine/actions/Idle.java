package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public class Idle extends Action {

    @Override
    public ActionKey getKey() {
        return ActionKey.IDLE;
    }

    @Override
    public Long durationFrames(GameEngine game, int actor) {
        return null;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return false;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        // do nothing
    }

    @Override
    public boolean isTurnable(GameEngine game, int actor) {
        return true;
    }
}
