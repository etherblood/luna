package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.MilliHealth;
import com.etherblood.luna.engine.PendingDelete;

public class Fallen extends Action {

    private final long aliveFrames;

    public Fallen(long aliveFrames) {
        this.aliveFrames = aliveFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.FALLEN;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > aliveFrames;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        MilliHealth health = game.getData().get(actor, MilliHealth.class);
        if (health == null || health.value() <= 0) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        EntityData data = game.getData();
        if (getElapsedFrames(game, actor) == aliveFrames) {
            MilliHealth health = data.get(actor, MilliHealth.class);
            if (health == null || health.value() <= 0) {
                data.set(actor, new PendingDelete(game.getFrame()));
            }
        }
    }
}
