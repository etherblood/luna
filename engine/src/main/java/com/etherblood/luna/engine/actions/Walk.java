package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Speed;

public class Walk extends Action {
    private static final int SPEED = 1000 / 60;

    private final long elapsedFrames;

    public Walk(long elapsedFrames) {
        this.elapsedFrames = elapsedFrames;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.WALK;
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
        EntityData data = game.getData();
        Direction direction = data.get(actor, Direction.class);
        data.set(actor, new Speed(direction.toLengthVector(SPEED)));
    }

    @Override
    public boolean isTurnable() {
        return true;
    }
}
