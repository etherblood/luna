package com.etherblood.luna.network.api.game;

import com.etherblood.luna.engine.GameEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlaybackBuffer {

    private final Map<Long, Set<GameEvent>> events = new HashMap<>();
    private long lockedFrame = -1;

    public synchronized boolean buffer(long frame, GameEvent event) {
        if (frame > lockedFrame) {
            Set<GameEvent> frameEvents = events.computeIfAbsent(frame, k -> new HashSet<>());
            return frameEvents.add(event);
        }
        return false;
    }

    public synchronized Set<GameEvent> peek(long frame) {
        Set<GameEvent> set = events.getOrDefault(frame, Collections.emptySet());
        return Collections.unmodifiableSet(set);
    }

    public synchronized void lockFrame(long frame) {
        lockedFrame = frame;
        events.keySet().removeIf(key -> key <= lockedFrame);
    }

    public long getLockedFrame() {
        return lockedFrame;
    }
}
