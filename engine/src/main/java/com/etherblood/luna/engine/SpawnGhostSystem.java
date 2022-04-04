package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class SpawnGhostSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        if (data.findByValue(new ModelKey("ghost")).isEmpty()) {
            int player = data.createEntity();
            engine.applyTemplate(player, "ghost");
            data.set(player, new Position(0, 10_000));
            data.set(player, Team.OPPONENTS);
        }
    }
}
