package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;

public class UpdateActorStateSystem implements GameSystem {

    private static final long DASH_DURATION_TICKS = 48;
    
    private static final long ATTACK1_DURATION_TICKS = 280;// allow any interrupt for frames 160+
    private static final long ATTACK1_DAMAGE_FRAME = 100;

    private static final long ATTACK2_DURATION_TICKS = 160;// allow any interrupt for frames 100+
    private static final long ATTACK2_DAMAGE_FRAME = 64;

    @Override
    public void tick(GameEngine engine) {
        EntityData data = engine.getData();
        for (int entity : data.list(ActorState.class)) {
            ActorState state = data.get(entity, ActorState.class);
            if (data.has(entity, Health.class) && data.get(entity, Health.class).value() <= 0) {
                if (ActorAction.DEATH.interrupts(state.action())) {
                    state = new ActorState(ActorAction.DEATH, Direction.NONE, engine.getFrame());
                }
            }
            if (state.action() == ActorAction.DASH && state.startFrame() + DASH_DURATION_TICKS <= engine.getFrame()) {
                state = new ActorState(ActorAction.IDLE, state.direction(), engine.getFrame());
            }
            if (state.action() == ActorAction.ATTACK1) {
                if (state.startFrame() + ATTACK1_DURATION_TICKS <= engine.getFrame()) {
                    state = new ActorState(ActorAction.IDLE, state.direction(), engine.getFrame());
                } else if (state.startFrame() + ATTACK1_DAMAGE_FRAME == engine.getFrame()) {
                    // TODO: deal damage on specific attack frame?
                    int damage = 20;

                    // placeholder
                    Vector2 attackPosition = data.get(entity, Position.class).vector();
                    for (int other : data.list(Health.class)) {
                        if (other == entity) {
                            continue;
                        }
                        Hitbox hitbox = data.get(other, Hitbox.class);
                        Position position = data.get(other, Position.class);
                        if (hitbox != null && position != null) {
                            if (hitbox.shape().translate(position.vector()).contains(attackPosition)) {
                                data.set(other, new Health(data.get(other, Health.class).value() - damage));
                            }
                        }
                    }
                }
            }
            if (state.action() == ActorAction.ATTACK2) {
                if (state.startFrame() + ATTACK2_DURATION_TICKS <= engine.getFrame()) {
                    state = new ActorState(ActorAction.IDLE, state.direction(), engine.getFrame());
                } else if (state.startFrame() + ATTACK2_DAMAGE_FRAME == engine.getFrame()) {
                    // TODO: deal damage on specific attack frame?
                    int damage = 10;

                    // placeholder
                    Vector2 attackPosition = data.get(entity, Position.class).vector();
                    for (int other : data.list(Health.class)) {
                        if (other == entity) {
                            continue;
                        }
                        Hitbox hitbox = data.get(other, Hitbox.class);
                        Position position = data.get(other, Position.class);
                        if (hitbox != null && position != null) {
                            if (hitbox.shape().translate(position.vector()).contains(attackPosition)) {
                                data.set(other, new Health(data.get(other, Health.class).value() - damage));
                            }
                        }
                    }
                }
            }

            PlayerInput input = data.get(entity, PlayerInput.class);
            if (input != null) {
                if (input.action().interrupts(state.action())) {
                    state = new ActorState(input.action(), input.direction(), engine.getFrame());
                } else if (state.action().isTurnable() && state.direction() != input.direction()) {
                    state = new ActorState(state.action(), input.direction(), state.startFrame());
                }
            }
            data.set(entity, state);
        }
    }
}
