package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.ActionKey;

public class TemplatesFactoryImpl implements TemplatesFactory {

    @Override
    public void apply(GameEngine game, int entity, String templateKey) {
        EntityData data = game.getData();
        switch (templateKey) {
            case "amara":
                data.set(entity, new Movebox(new Rectangle(-250, -250, 500, 500)));
                data.set(entity, new Hitbox(new Circle(0, 0, 250)));
                data.set(entity, new ActorState(ActionKey.IDLE, game.getFrame()));
                data.set(entity, Direction.DOWN);
                data.set(entity, new Health(100));
                data.set(entity, new ActorKey("amara"));

                break;
            default:
                throw new AssertionError(templateKey);
        }
    }
}
