package com.etherblood.luna.engine.controlflow;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.PendingDelete;
import com.etherblood.luna.engine.PlayerId;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Rectangle;

public class TriggerSystem implements GameSystem {
    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int entity : data.list(Triggerbox.class)) {
            Triggerbox triggerbox = data.get(entity, Triggerbox.class);
            Rectangle area = triggerbox.area();
            Position position = data.get(entity, Position.class);
            if (position != null) {
                area = area.translate(position.vector());
            }

            for (int player : data.list(PlayerId.class)) {
                Position playerPosition = data.get(player, Position.class);
                if (area.containsIncludeBounds(playerPosition.vector())) {

                    for (int other : data.findByValue(new WaitForTrigger(triggerbox.triggerId()))) {
                        data.remove(other, WaitForTrigger.class);
                    }
                    data.set(entity, new PendingDelete(game.getFrame()));
                    break;
                }
            }
        }
    }
}
