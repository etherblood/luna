package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Speed;

public class Dash extends Action {
    private static final long DURATION_FRAMES = 48;
    private static final int SPEED_MILLIMETRES_PER_SECOND = 3000;

    @Override
    public ActionKey getKey() {
        return ActionKey.DASH;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > DURATION_FRAMES;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return 1;
    }

    @Override
    public void update(GameEngine game, int actor) {
        EntityData data = game.getData();
        data.set(actor, new Speed(SPEED_MILLIMETRES_PER_SECOND / game.getRules().getFramesPerSecond()));
    }

    @Override
    public void cleanup(GameEngine game, int actor) {
        game.getData().remove(actor, Speed.class);
    }
}
