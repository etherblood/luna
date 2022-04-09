package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.actions.data.ActiveCooldown;

public class ActionCooldownExpireSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.findByValue(new ActiveCooldown(game.getFrame()))) {
            data.remove(entity, ActiveCooldown.class);
        }

        for (int entity : data.list(ActiveCooldown.class)) {
            if (data.get(entity, ActiveCooldown.class).endFrame() < game.getFrame()) {
                throw new AssertionError("ActiveCooldown alive after end frame.");
            }
        }
    }
}
