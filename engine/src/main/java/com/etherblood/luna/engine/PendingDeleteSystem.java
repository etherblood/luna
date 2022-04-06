package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class PendingDeleteSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.findByValue(new PendingDelete(engine.getFrame()))) {
            for (Class<?> component : data.getRegisteredClasses()) {
                data.remove(entity, component);
            }
        }
        for (int entity : data.list(PendingDelete.class)) {
            if (data.get(entity, PendingDelete.class).frame() < engine.getFrame()) {
                throw new AssertionError("Entity alive after PendingDelete frame.");
            }
        }
    }
}
