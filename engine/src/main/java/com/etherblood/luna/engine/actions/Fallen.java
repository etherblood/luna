package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Health;

public class Fallen extends Action {
    private static final long ALIVE_FRAMES = 300;

    @Override
    public ActionKey getKey() {
        return ActionKey.FALLEN;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > ALIVE_FRAMES;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        Health health = game.getData().get(actor, Health.class);
        if (health == null || health.value() <= 0) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        EntityData data = game.getData();
        if (getElapsedFrames(game, actor) == ALIVE_FRAMES) {
            Health health = data.get(actor, Health.class);
            if (health == null || health.value() <= 0) {
                for (Class<?> component : data.getRegisteredClasses()) {
                    data.remove(actor, component);
                }
            }
        }
    }
}
