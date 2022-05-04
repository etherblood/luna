package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class PendingDeleteSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(PendingDeleteOwner.class)) {
            PendingDeleteOwner pendingDeleteOwner = data.get(entity, PendingDeleteOwner.class);
            OwnedBy ownedBy = data.get(entity, OwnedBy.class);
            data.set(ownedBy.owner(), new PendingDelete(pendingDeleteOwner.frame()));
            data.remove(entity, PendingDeleteOwner.class);
        }

        for (int entity : data.findByValue(new PendingDelete(game.getFrame()))) {
            deleteRecursively(data, entity);
        }
        for (int entity : data.list(PendingDelete.class)) {
            if (data.get(entity, PendingDelete.class).frame() < game.getFrame()) {
                throw new AssertionError("Entity alive after PendingDelete frame.");
            }
        }
    }

    private void deleteRecursively(EntityData data, int entity) {
        for (Class<?> component : data.getRegisteredClasses()) {
            data.remove(entity, component);
        }
        for (int other : data.findByValue(new CascadeDelete(entity))) {
            deleteRecursively(data, other);
        }
    }
}
