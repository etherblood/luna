package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.Collection;
import java.util.List;

public class GameEngine {

    private final GameRules rules;
    private final long startEpochMillis;
    private long tick;
    private final EntityData data;
    private final List<GameSystem> systems;

    public GameEngine(GameRules rules, long startEpochMillis, long tick) {
        this(rules, startEpochMillis, new EntityDataImpl(rules.getComponentTypes()), tick);
    }

    public GameEngine(GameRules rules, long startEpochMillis, EntityData data, long tick) {
        this.rules = rules;
        this.startEpochMillis = startEpochMillis;
        this.data = data;
        this.systems = rules.getSystems();
        this.tick = tick;
    }

    public void tick(Collection<GameEvent> events) {
        // TODO: make sure order of events does not matter!
        for (GameEvent event : events) {
            if (event.input() != null) {
                long player = event.input().player();
                for (int entity : data.findByValue(new PlayerId(player))) {
                    data.set(entity, event.input());
                }
            }
            if (event.join() != null) {
                if (event.join().enter()) {
                    if (data.findByValue(new PlayerId(event.join().playerId())).isEmpty()) {
                        int player = data.createEntity();
                        data.set(player, new PlayerId(event.join().playerId()));
                        data.set(player, new PlayerName(event.join().playerName()));
                        data.set(player, new Movebox(new Rectangle(-250, -250, 500, 500)));
                        data.set(player, new Position(0, 0));
                        data.set(player, new Speed(0, 0));
                        data.set(player, new ActorState(ActorAction.IDLE, Direction.NONE, 0));
                    }
                } else {
//                    List<Integer> playerEntities = data.findByValue(new PlayerId(event.join().playerId()));
//                    List<Integer> characterEntities = data.findByValue(new OwnedBy(event.join().playerId()));
//                    for (Class<?> component : data.getRegisteredClasses()) {
//                        for (Integer entity : playerEntities) {
//                            data.remove(entity, component);
//                        }
//                        for (Integer entity : characterEntities) {
//                            data.remove(entity, component);
//                        }
//                    }
                }
            }
        }
        tick++;
        for (GameSystem system : systems) {
            system.tick(this);
        }
    }

    public GameRules getRules() {
        return rules;
    }

    public long getFrame() {
        return tick;
    }

    public EntityData getData() {
        return data;
    }

    public long getStartEpochMillis() {
        return startEpochMillis;
    }

}
