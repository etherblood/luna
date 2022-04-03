package com.etherblood.luna.engine.actions;

import com.etherblood.luna.engine.ActorState;
import com.etherblood.luna.engine.GameEngine;

public abstract class Action {

    public abstract ActionKey getKey();

    public abstract boolean hasEnded(GameEngine game, int actor);

    public boolean isInterruptedBy(GameEngine game, int actor, ActionKey key) {
        return getKey() != key && key.interruptStrength() >= interruptResistance(game, actor);
    }

    public boolean isTurnable(GameEngine game, int actor) {
        return false;
    }

    public abstract void update(GameEngine game, int actor);

    protected abstract int interruptResistance(GameEngine game, int actor);

    protected long getElapsedFrames(GameEngine game, int actor) {
        return game.getFrame() - game.getData().get(actor, ActorState.class).startFrame();
    }

    public void cleanup(GameEngine game, int actor) {
        // do nothing as default
    }

}
