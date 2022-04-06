package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.actions.ActionKey;
import com.etherblood.luna.engine.behaviors.GhostBehavior;
import java.util.Map;

public class TemplatesFactoryImpl implements TemplatesFactory {

    @Override
    public void apply(GameEngine game, int entity, String templateKey) {
        EntityData data = game.getData();
        int fps = game.getRules().getFramesPerSecond();
        switch (templateKey) {
            case "amara":
                data.set(entity, new Movebox(new Rectangle(-250, -250, 500, 500)));
                data.set(entity, new Hitbox(new Circle(0, 0, 250)));
                data.set(entity, new ActorState("amara.idle", game.getFrame()));
                data.set(entity, new SkillSet(
                        Map.of(
                                ActionKey.IDLE, "amara.idle",
                                ActionKey.WALK, "amara.walk",
                                ActionKey.DASH, "amara.dash",
                                ActionKey.ATTACK1, "amara.gaze_of_darkness",
                                ActionKey.ATTACK2, "amara.blade_of_chaos"
                        )
                ));
                data.set(entity, Direction.UP);
                data.set(entity, new MilliHealth(100_000));
                data.set(entity, new MilliHealth(1_000_000_000));// temporary for testing
                data.set(entity, new ModelKey("amara"));
                break;
            case "ghost":
                data.set(entity, new Movebox(new Rectangle(-250, -250, 500, 500)));
                data.set(entity, new Hitbox(new Circle(0, 0, 250)));
                data.set(entity, new ActorState("ghost.idle", game.getFrame()));
                data.set(entity, new SkillSet(
                        Map.of(
                                ActionKey.IDLE, "ghost.idle",
                                ActionKey.WALK, "ghost.fly_forward",
                                ActionKey.ATTACK1, "ghost.melee_attack"
                        )
                ));
                data.set(entity, Direction.DOWN);
                data.set(entity, new MilliHealth(100_000));
                data.set(entity, new ModelKey("ghost"));
                data.set(entity, new GhostBehavior());
                break;
            case "gaze_of_darkness":
                data.set(entity, new Damagebox(new Circle(0, 0, 1_000), 10_000 / fps));// TODO: rounding error is introduced to damage here, fixable?
                data.set(entity, new PendingDelete(game.getFrame() + 5 * fps));
                data.set(entity, new ModelKey("gaze_of_darkness"));
                break;
            case "blade_of_chaos":
                data.set(entity, new Damagebox(new Circle(0, 0, 500), 25_000 / fps));// TODO: rounding error is introduced to damage here, fixable?
                data.set(entity, new PendingDelete(game.getFrame() + 1 * fps));
                int milliMetresPerFrame = 6_000 / game.getRules().getFramesPerSecond();
                data.set(entity, new Speed(milliMetresPerFrame));
                data.set(entity, new ModelKey("blade_of_chaos"));
                break;
            default:
                throw new AssertionError(templateKey);
        }
    }
}
