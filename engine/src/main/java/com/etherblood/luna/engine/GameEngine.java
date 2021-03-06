package com.etherblood.luna.engine;

import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GameEngine {

    private final UUID id;
    private final GameRules rules;
    private final long startEpochMillis;
    private long frame;
    private final EntityData data;
    private final List<GameSystem> systems;
    private final TemplatesFactory templates;

    public GameEngine(UUID id, GameRules rules, long startEpochMillis, long frame) {
        this(id, rules, startEpochMillis, new EntityDataImpl(rules.getComponentTypes()), frame);
    }

    public GameEngine(UUID id, GameRules rules, long startEpochMillis, EntityData data, long frame) {
        this.id = id;
        this.rules = rules;
        this.startEpochMillis = startEpochMillis;
        this.data = data;
        this.systems = rules.getSystems();
        this.templates = rules.getTemplates();
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
            if (event.join() != null) {
                if (!event.join().enter()) {
                    for (int entity : data.findByValue(new PlayerId(event.join().playerId()))) {
                        data.set(entity, new PendingDelete(frame));
                    }
                }
            }
        }
        for (GameEvent event : events) {
            if (event.join() != null) {
                if (event.join().enter()) {
                    int player = data.createEntity();
                    applyTemplate(player, event.join().actorTemplate());
                    data.set(player, new PlayerId(event.join().playerId()));
                    data.set(player, new ActorName(event.join().playerName()));
                    data.set(player, new Position(0, 0));
                    data.set(player, Team.PLAYERS);
                }
            }
        }
        for (GameEvent event : events) {
            if (event.input() != null) {
                long player = event.input().player();
                for (int entity : data.findByValue(new PlayerId(player))) {
                    data.set(entity, new ActorInput(event.input().direction(), event.input().action()));
                }
            }
        }
        for (GameSystem system : systems) {
            system.tick(this);
        }
        frame++;
    }

    public void applyTemplate(int entity, String template) {
        templates.apply(this, entity, template);
    }

    public UUID getId() {
        return id;
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

    public TemplatesFactory getTemplates() {
        return templates;
    }
}
