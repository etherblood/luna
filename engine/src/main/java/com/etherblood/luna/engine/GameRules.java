package com.etherblood.luna.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameRules {

    private static final Map<String, GameRules> RULES_MAP;
    private static final String DEFAULT_RULES_ID = "default";

    static {
        RULES_MAP = Map.of(DEFAULT_RULES_ID, new GameRules(
                DEFAULT_RULES_ID,
                Set.of(
                        Position.class,
                        Speed.class,
                        Movebox.class,
                        Hitbox.class,
                        ActorState.class,
                        PlayerInput.class,
                        PlayerId.class,
                        PlayerName.class,
                        Health.class
                ),
                List.of(
                        new ApplyActionSystem(),
                        new MovementSystem(),
                        new UpdateActorStateSystem()
                )));
    }

    private final String id;
    private final Set<Class<?>> componentTypes;
    private final List<GameSystem> systems;

    public GameRules(String id, Set<Class<?>> componentTypes, List<GameSystem> systems) {
        this.id = id;
        this.componentTypes = componentTypes;
        this.systems = systems;
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
}
