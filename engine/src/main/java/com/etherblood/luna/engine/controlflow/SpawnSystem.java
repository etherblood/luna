package com.etherblood.luna.engine.controlflow;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.PendingDelete;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Team;
import com.etherblood.luna.engine.actions.data.ActiveCooldown;
import com.etherblood.luna.engine.actions.data.BaseCooldown;

public class SpawnSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(Spawner.class)) {
            if (data.has(entity, WaitForTrigger.class)) {
                continue;
            }
            if (data.has(entity, ActiveCooldown.class)) {
                continue;
            }
            Spawner spawner = data.get(entity, Spawner.class);

            int spawn = data.createEntity();
            game.applyTemplate(spawn, spawner.spawnTemplate());
            Position position = data.get(entity, Position.class);
            if (position != null) {
                data.set(spawn, position);
            }
            Team team = data.get(entity, Team.class);
            if (team != null) {
                data.set(spawn, team);
            }

            BaseCooldown cooldown = data.get(entity, BaseCooldown.class);
            if (cooldown != null) {
                data.set(entity, new ActiveCooldown(game.getFrame() + cooldown.frames()));
            } else {
                data.set(entity, new PendingDelete(game.getFrame()));
            }
        }
//        if (game.getFrame() % 569 == 0 && data.findByValue(new ModelKey("ghost")).size() < 5) {
//            int player = data.createEntity();
//            game.applyTemplate(player, "ghost");
//            data.set(player, new Position(0, 20_000));
//            data.set(player, Team.OPPONENTS);
//        }
    }
}
