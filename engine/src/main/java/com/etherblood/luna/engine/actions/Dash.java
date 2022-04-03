package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Speed;

public class Dash extends Action {
    private static final long DURATION_FRAMES = 48;
    private static final int SPEED = 3000 / 60;

    private final long elapsedFrames;

    public Dash(long elapsedFrames) {
        this.elapsedFrames = elapsedFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.DASH;
    }

    @Override
    public boolean hasEnded() {
        return elapsedFrames > DURATION_FRAMES;
    }

    @Override
    protected int interruptResistance() {
        return 1;
    }

    @Override
    public void update(GameEngine game, int actor) {
        EntityData data = game.getData();
        Direction direction = data.get(actor, Direction.class);
        data.set(actor, new Speed(direction.toLengthVector(SPEED)));
    }
}
