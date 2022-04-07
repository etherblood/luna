package com.etherblood.luna.engine.damage;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.Circle;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.Position;
import java.util.ArrayList;
import java.util.List;

public class DamageboxCollisionSystem implements GameSystem {

    private final boolean cacheCollisions;
    private final List<DamageCollisionPair> cachedCollisions = new ArrayList<>();

    public DamageboxCollisionSystem(boolean cacheCollisions) {
        this.cacheCollisions = cacheCollisions;
    }

    @Override
    public void tick(GameEngine engine) {
        if (!cacheCollisions) {
            return;
        }
        if (!cachedCollisions.isEmpty()) {
            // we want to make sure no state carries over between frames
            throw new AssertionError("cached collisions must be cleared after use.");
        }
        cachedCollisions.addAll(calculateCollisions(engine));
    }

    public List<DamageCollisionPair> getCachedCollisions() {
        if (cacheCollisions) {
            return cachedCollisions;
        }
        return null;
    }

    public List<DamageCollisionPair> calculateCollisions(GameEngine engine) {
        List<DamageCollisionPair> collisions = new ArrayList<>();
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
                        collisions.add(new DamageCollisionPair(entity, other));
                    }
                }
            }
        }
        return collisions;
    }
}
