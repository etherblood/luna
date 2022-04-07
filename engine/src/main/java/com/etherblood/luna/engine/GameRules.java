package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionFactory;
import com.etherblood.luna.engine.actions.Attack1Cooldown;
import com.etherblood.luna.engine.actions.Attack2Cooldown;
import com.etherblood.luna.engine.behaviors.GhostBehavior;
import com.etherblood.luna.engine.behaviors.GhostBehaviorSystem;
import com.etherblood.luna.engine.damage.Damagebox;
import com.etherblood.luna.engine.damage.DamageboxCollisionSystem;
import com.etherblood.luna.engine.damage.DamageboxSystem;
import com.etherblood.luna.engine.damage.DeleteSelfAfterDamageTrigger;
import com.etherblood.luna.engine.damage.Hitbox;
import com.etherblood.luna.engine.damage.MilliHealth;
import com.etherblood.luna.engine.damage.Team;
import com.etherblood.luna.engine.movement.Movebox;
import com.etherblood.luna.engine.movement.MovementSystem;
import com.etherblood.luna.engine.movement.Obstaclebox;
import com.etherblood.luna.engine.movement.Speed;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GameRules {

    private static final Map<String, Supplier<GameRules>> RULES_MAP;
    private static final String DEFAULT_RULES_ID = "default";

    private final String id;
    private final Set<Class<?>> componentTypes;
    private final List<GameSystem> systems;
    private final TemplatesFactory templates;
    private final int framesPerSecond;

    static {
        RULES_MAP = Map.of(DEFAULT_RULES_ID, () -> {
            ActionFactory actionFactory = new ActionFactory();
            DamageboxCollisionSystem collisionSystem = new DamageboxCollisionSystem(true);
            return new GameRules(
                    DEFAULT_RULES_ID,
                    Set.of(
                            Position.class,
                            Speed.class,
                            Movebox.class,
                            Obstaclebox.class,
                            Hitbox.class,
                            Damagebox.class,
                            ActorState.class,
                            ActorInput.class,
                            PlayerId.class,
                            ActorName.class,
                            Direction.class,
                            MilliHealth.class,
                            ModelKey.class,
                            PendingDelete.class,
                            Team.class,
                            GhostBehavior.class,
                            SkillSet.class,
                            Attack1Cooldown.class,
                            Attack2Cooldown.class,
                            DeleteSelfAfterDamageTrigger.class
                    ),
                    List.of(
                            collisionSystem,// cache collisions
                            new SpawnGhostSystem(),
                            new GhostBehaviorSystem(),
                            new UpdateActorStateSystem(actionFactory),
                            new ApplyActionSystem(actionFactory),
                            new MovementSystem(),
                            new DamageboxSystem(collisionSystem),
                            new PendingDeleteSystem(),
                            new CooldownExpireSystem()
                    ),
                    new TemplatesFactoryImpl(),
                    60);
        });
    }

    public GameRules(String id, Set<Class<?>> componentTypes, List<GameSystem> systems, TemplatesFactory templates, int framesPerSecond) {
        this.id = id;
        this.componentTypes = componentTypes;
        this.systems = systems;
        this.templates = templates;
        this.framesPerSecond = framesPerSecond;
    }

    public static GameRules getDefault() {
        return get(DEFAULT_RULES_ID);
    }

    public static GameRules get(String id) {
        Supplier<GameRules> supplier = RULES_MAP.get(id);
        if (supplier == null) {
            throw new NullPointerException("No rules found for id " + id);
        }
        return supplier.get();
    }

    public GameEngine createGame() {
        return new GameEngine(this, System.currentTimeMillis(), 0);
    }

    public String getId() {
        return id;
    }

    public Set<Class<?>> getComponentTypes() {
        return componentTypes;
    }

    public List<GameSystem> getSystems() {
        return systems;
    }

    public TemplatesFactory getTemplates() {
        return templates;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }
}
