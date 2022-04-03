package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class PendingDeleteSystem implements GameSystem {
    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.findByValue(new PendingDelete(engine.getFrame() - 1))) {
            for (Class<?> component : data.getRegisteredClasses()) {
                data.remove(entity, component);
            }
        }
    }
}
