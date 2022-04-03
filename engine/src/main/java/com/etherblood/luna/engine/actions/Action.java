package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.GameEngine;

public abstract class Action {

    public abstract ActionKey getKey();

    public abstract boolean hasEnded();

    public boolean isInterruptedBy(ActionKey key) {
        return getKey() != key && isInterruptedBy(key.interruptStrength());
    }

    protected boolean isInterruptedBy(int interruptStrength) {
        return interruptStrength >= interruptResistance();
    }

    protected abstract int interruptResistance();

    public boolean isTurnable() {
        return false;
    }

    public abstract void update(GameEngine game, int actor);
    
}
