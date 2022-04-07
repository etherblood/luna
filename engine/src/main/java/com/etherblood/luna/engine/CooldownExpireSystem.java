package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.Attack1Cooldown;
import com.etherblood.luna.engine.actions.Attack2Cooldown;

public class CooldownExpireSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.findByValue(new Attack1Cooldown(engine.getFrame()))) {
            data.remove(entity, Attack1Cooldown.class);
        }
        for (int entity : data.findByValue(new Attack2Cooldown(engine.getFrame()))) {
            data.remove(entity, Attack2Cooldown.class);
        }

        for (int entity : data.list(Attack1Cooldown.class)) {
            if (data.get(entity, Attack1Cooldown.class).endsFrame() < engine.getFrame()) {
                throw new AssertionError("Attack1Cooldown alive after end frame.");
            }
        }
        for (int entity : data.list(Attack2Cooldown.class)) {
            if (data.get(entity, Attack2Cooldown.class).endsFrame() < engine.getFrame()) {
                throw new AssertionError("Attack2Cooldown alive after end frame.");
            }
        }
    }
}
