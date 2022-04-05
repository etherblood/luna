package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionFactory;
import com.etherblood.luna.engine.behaviors.GhostBehavior;
import com.etherblood.luna.engine.behaviors.GhostBehaviorSystem;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameRules {

    private static final Map<String, GameRules> RULES_MAP;
    private static final String DEFAULT_RULES_ID = "default";

    private final String id;
    private final Set<Class<?>> componentTypes;
    private final List<GameSystem> systems;
    private final TemplatesFactory templates;
    private final int framesPerSecond;

    static {
        ActionFactory actionFactory = new ActionFactory();
        RULES_MAP = Map.of(DEFAULT_RULES_ID, new GameRules(
                DEFAULT_RULES_ID,
                Set.of(
                        Position.class,
                        Speed.class,
                        Movebox.class,
                        Hitbox.class,
                        Damagebox.class,
                        ActorState.class,
                        ActorInput.class,
                        PlayerId.class,
                        PlayerName.class,
                        Direction.class,
                        MilliHealth.class,
                        ModelKey.class,
                        PendingDelete.class,
                        Team.class,
                        GhostBehavior.class,
                        SkillSet.class
                ),
                List.of(
                        new SpawnGhostSystem(),
                        new GhostBehaviorSystem(),
                        new UpdateActorStateSystem(actionFactory),
                        new ApplyActionSystem(actionFactory),
                        new MovementSystem(),
                        new DamageboxSystem(),
                        new PendingDeleteSystem()
                ),
                new TemplatesFactoryImpl(),
                60));
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
        return RULES_MAP.get(id);
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
