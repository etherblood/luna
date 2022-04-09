package com.etherblood.luna.engine.actions;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.ActiveAction;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameSystem;
import com.etherblood.luna.engine.OwnedBy;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Team;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.data.ActionEvent;
import com.etherblood.luna.engine.actions.data.ActionRange;

public class ActionEventSystem implements GameSystem {

    @Override
    public void tick(GameEngine game) {
        EntityData data = game.getData();
        for (int actor : data.list(ActiveAction.class)) {
            ActiveAction activeAction = data.get(actor, ActiveAction.class);
            int action = activeAction.action();
            ActionEvent event = data.get(action, ActionEvent.class);
            if (event != null && activeAction.startFrame() + event.frame() == game.getFrame()) {
                int entity = data.createEntity();
                game.applyTemplate(entity, event.template());
                Vector2 targetPosition = data.get(actor, Position.class).vector();
                ActionRange range = data.get(action, ActionRange.class);
                Direction actorDirection = data.get(actor, Direction.class);
                if (range != null) {
                    targetPosition = targetPosition.add(actorDirection.toLengthVector(range.milliMetresRange()));
                }
                data.set(entity, new OwnedBy(actor));
                data.set(entity, new Position(targetPosition));
                data.set(entity, actorDirection);
                Team team = data.get(actor, Team.class);
                if (team != null) {
                    data.set(entity, team);
                }
            }
        }
    }
}
