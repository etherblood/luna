package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.damage.Team;

public class SpawnGhostSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        if (engine.getFrame() % 569 == 0 && data.findByValue(new ModelKey("ghost")).size() < 5) {
            int player = data.createEntity();
            engine.applyTemplate(player, "ghost");
            data.set(player, new Position(0, 20_000));
            data.set(player, Team.OPPONENTS);
        }
    }
}
