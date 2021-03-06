package com.etherblood.luna.engine.damage;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.PendingDelete;
import java.util.List;

public class DamageboxSystem implements GameSystem {

    private final DamageboxCollisionSystem collisionSystem;

    public DamageboxSystem(DamageboxCollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        List<DamageCollisionPair> cachedCollisions = collisionSystem.getCachedCollisions();
        List<DamageCollisionPair> newCollisions = collisionSystem.calculateCollisions(game);

        for (DamageCollisionPair collision : newCollisions) {
            int entity = collision.damageEntity();
            int other = collision.hurtEntity();

            boolean triggered = false;
            Damagebox damagebox = data.get(entity, Damagebox.class);
            if (damagebox.trigger() == DamageTrigger.PER_FRAME) {
                data.set(other, new MilliHealth(data.get(other, MilliHealth.class).value() - damagebox.milliDamage()));
                triggered = true;
            } else if (damagebox.trigger() == DamageTrigger.ON_COLLISION && !cachedCollisions.contains(collision)) {
                data.set(other, new MilliHealth(data.get(other, MilliHealth.class).value() - damagebox.milliDamage()));
                triggered = true;
            }
            if (triggered && data.has(entity, DeleteSelfAfterDamageTrigger.class)) {
                data.set(entity, new PendingDelete(game.getFrame()));
            }
        }
        cachedCollisions.clear();
    }
}
