package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.movement.Speed;

public class Dash extends Action {

    private final long durationFrames;
    private final long speedMillimetresPerSecond;

    public Dash(long durationFrames, long speedMillimetresPerSecond) {
        this.durationFrames = durationFrames;
        this.speedMillimetresPerSecond = speedMillimetresPerSecond;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.DASH;
    }

    @Override
    public Long cooldownFrames(GameEngine game, int actor) {
        return null;
    }

    @Override
    public Long durationFrames(GameEngine game, int actor) {
        return null;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > durationFrames;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return 1;
    }

    @Override
    public void update(GameEngine game, int actor) {
        EntityData data = game.getData();
        data.set(actor, new Speed(speedMillimetresPerSecond / game.getRules().getFramesPerSecond()));
    }

    @Override
    public void cleanup(GameEngine game, int actor) {
        game.getData().remove(actor, Speed.class);
    }
}
