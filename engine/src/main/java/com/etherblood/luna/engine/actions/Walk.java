package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Speed;

public class Walk extends Action {
    private final int speedMillimetresPerSecond;

    public Walk(int speed_millimetres_per_second) {
        speedMillimetresPerSecond = speed_millimetres_per_second;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.WALK;
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
        EntityData data = game.getData();
        data.set(actor, new Speed(speedMillimetresPerSecond / game.getRules().getFramesPerSecond()));
    }

    @Override
    public boolean isTurnable(GameEngine game, int actor) {
        return true;
    }

    @Override
    public void cleanup(GameEngine game, int actor) {
        game.getData().remove(actor, Speed.class);
    }
}
