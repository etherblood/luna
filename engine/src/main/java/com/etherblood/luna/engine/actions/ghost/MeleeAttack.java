package com.etherblood.luna.engine.actions.ghost;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Circle;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.PendingDelete;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.Action;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.damage.DamageTrigger;
import com.etherblood.luna.engine.damage.Damagebox;
import com.etherblood.luna.engine.damage.Team;

public class MeleeAttack extends Action {
    private final long damageFrame;
    private final long interruptResistFrames;
    private final long durationFrames;
    private final long range;
    private final long radius;
    private final long milliDamage;

    public MeleeAttack(long damageFrame, long interruptResistFrames, long durationFrames, long range, long radius, long milliDamage) {
        this.damageFrame = damageFrame;
        this.interruptResistFrames = interruptResistFrames;
        this.durationFrames = durationFrames;
        this.range = range;
        this.radius = radius;
        this.milliDamage = milliDamage;
    }

    @Override
    public ActionKey getKey() {
        return ActionKey.ATTACK1;
    }

    @Override
    public Long cooldownFrames(GameEngine game, int actor) {
        return null;
    }

    @Override
    public Long durationFrames(GameEngine game, int actor) {
        return durationFrames;
    }

    @Override
    protected int interruptResistance(GameEngine game, int actor) {
        return getElapsedFrames(game, actor) < interruptResistFrames ? 2 : 0;
    }

    @Override
    public void update(GameEngine game, int actor) {
        if (getElapsedFrames(game, actor) == damageFrame) {
            EntityData data = game.getData();
            int entity = data.createEntity();
            data.set(entity, new Damagebox(new Circle(0, 0, radius), DamageTrigger.PER_FRAME, milliDamage));
            data.set(entity, new PendingDelete(game.getFrame()));
            Vector2 actorPosition = data.get(actor, Position.class).vector();
            Direction actorDirection = data.get(actor, Direction.class);
            data.set(entity, new Position(actorPosition.add(actorDirection.toLengthVector(range))));
            data.set(entity, actorDirection);
            Team team = data.get(actor, Team.class);
            if (team != null) {
                data.set(entity, team);
            }
        }
    }
}
