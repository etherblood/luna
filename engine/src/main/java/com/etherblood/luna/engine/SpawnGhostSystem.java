package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class SpawnGhostSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        if (game.getFrame() % 569 == 0 && data.findByValue(new ModelKey("ghost")).size() < 5) {
            int player = data.createEntity();
            game.applyTemplate(player, "ghost");
            data.set(player, new Position(0, 20_000));
            data.set(player, Team.OPPONENTS);
        }
    }
}
