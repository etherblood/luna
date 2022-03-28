package com.etherblood.luna.network.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlaybackBuffer<T> {

    private final Map<Long, Set<T>> actions = new HashMap<>();
    private long clearedFrame = -1;

    public boolean buffer(long frame, T action) {
        if (clearedFrame >= frame) {
            return false;
        }
        actions.computeIfAbsent(frame, k -> new HashSet<>()).add(action);
        return true;
    }

    public Set<T> peek(long frame) {
        Set<T> set = actions.getOrDefault(frame, Collections.emptySet());
        return Collections.unmodifiableSet(set);
    }

    public void clear(long frame) {
        clearedFrame = frame;
        actions.keySet().removeIf(key -> key <= frame);
    }
}
