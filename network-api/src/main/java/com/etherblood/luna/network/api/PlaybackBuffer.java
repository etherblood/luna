package com.etherblood.luna.network.api;

import com.etherblood.luna.engine.GameEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlaybackBuffer {

    private final Map<Long, Set<GameEvent>> events = new HashMap<>();
    private long clearedFrame = -1;

    public boolean buffer(long frame, GameEvent event) {
        if (clearedFrame >= frame) {
            return false;
        }
        events.computeIfAbsent(frame, k -> new HashSet<>()).add(event);
        return true;
    }

    public Set<GameEvent> peek(long frame) {
        Set<GameEvent> set = events.getOrDefault(frame, Collections.emptySet());
        return Collections.unmodifiableSet(set);
    }

    public void clear(long frame) {
        clearedFrame = frame;
        events.keySet().removeIf(key -> key <= frame);
    }
}
