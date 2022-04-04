package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class DamageboxSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(Damagebox.class)) {
            Position position = data.get(entity, Position.class);
            Damagebox damagebox = data.get(entity, Damagebox.class);
            Circle damageCircle = damagebox.shape().translate(position.vector());
            Team team = data.get(entity, Team.class);

            for (int other : data.list(Hitbox.class)) {
                if (entity == other) {
                    continue;
                }
                Team otherTeam = data.get(other, Team.class);
                if (team != null && team.equals(otherTeam)) {
                    continue;
                }
                Position otherPosition = data.get(other, Position.class);
                if (otherPosition != null) {
                    Circle hitbox = data.get(other, Hitbox.class).shape().translate(otherPosition.vector());
                    if (damageCircle.intersects(hitbox)) {
                        data.set(other, new MilliHealth(data.get(other, MilliHealth.class).value() - damagebox.milliDamage()));
                    }
                }
            }
        }
    }
}
