package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameEngine {

    private final GameRules rules;
    private final long startEpochMillis;
    private long frame;
    private final EntityData data;
    private final List<GameSystem> systems;

    public GameEngine(GameRules rules, long startEpochMillis, long frame) {
        this(rules, startEpochMillis, new EntityDataImpl(rules.getComponentTypes()), frame);
    }

    public GameEngine(GameRules rules, long startEpochMillis, EntityData data, long frame) {
        this.rules = rules;
        this.startEpochMillis = startEpochMillis;
        this.data = data;
        this.systems = rules.getSystems();
        this.frame = frame;
    }

    public void tick(Collection<GameEvent> events) {
        // TODO: make sure order of events does not matter!
        ArrayList<GameEvent> list = new ArrayList<>(events);
        for (int i = 0; i < list.size() - 1; i++) {
            GameEvent a = list.get(i);
            if (a.input() == null) {
                continue;
            }
            for (int j = i + 1; j < list.size(); j++) {
                GameEvent b = list.get(j);
                if (b.input() == null) {
                    continue;
                }
                if (a.input().player() == b.input().player()) {
                    throw new IllegalArgumentException("Multiple inputs by player " + a.input().player());
                }
            }
        }

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
                        data.set(player, new Hitbox(new Circle(0, 0, 250)));
                        data.set(player, new Position(0, 0));
                        data.set(player, new Speed(0, 0));
                        data.set(player, new ActorState(ActorAction.IDLE, frame));
                        data.set(player, Direction.DOWN);
                        data.set(player, new Health(100));
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
        frame++;
        for (GameSystem system : systems) {
            system.tick(this);
        }
    }

    public GameRules getRules() {
        return rules;
    }

    public long getFrame() {
        return frame;
    }

    public EntityData getData() {
        return data;
    }

    public long getStartEpochMillis() {
        return startEpochMillis;
    }

}
