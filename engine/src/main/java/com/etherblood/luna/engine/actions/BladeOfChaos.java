package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;

public class BladeOfChaos extends Action {
    private static final long DAMAGE_FRAME = 64;
    private static final long INTERRUPT_RESIST_FRAMES = 100;
    private static final long DURATION_FRAMES = 160;
    private static final int RANGE = 500;

    @Override
    public ActionKey getKey() {
        return ActionKey.ATTACK2;
    }

    @Override
    public boolean hasEnded(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) > DURATION_FRAMES;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) < INTERRUPT_RESIST_FRAMES ? 2 : 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        if (getElapsedFrames(game, actor) == DAMAGE_FRAME) {
            EntityData data = game.getData();
            int entity = data.createEntity();
            game.applyTemplate(entity, "blade_of_chaos");
            Vector2 actorPosition = data.get(actor, Position.class).vector();
            Direction actorDirection = data.get(actor, Direction.class);
            data.set(entity, new Position(actorPosition.add(actorDirection.toLengthVector(RANGE))));
            data.set(entity, actorDirection);
        }
    }
}
