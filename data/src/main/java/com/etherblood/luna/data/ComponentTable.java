package com.etherblood.luna.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ComponentTable<T> {

    private final Map<Integer, T> table = new HashMap<>();
    private transient Map<T, Set<Integer>> index;

    public T get(int entity) {
        return table.get(entity);
    }

    public void set(int entity, T value) {
        T removed = table.put(entity, value);
        if (index != null) {
            removeFromIndex(entity, removed);
            addToIndex(entity, value);
        }
    }

    public void remove(int entity) {
        T removed = table.remove(entity);
        if (index != null) {
            removeFromIndex(entity, removed);
        }
    }

    public List<Integer> list() {
        return List.copyOf(table.keySet());
    }

    public List<Integer> findByValue(T value) {
        if (index == null) {
            index = new HashMap<>();
            for (Map.Entry<Integer, T> entry : table.entrySet()) {
                addToIndex(entry.getKey(), entry.getValue());
            }
        }
        return List.copyOf(index.getOrDefault(value, Collections.emptySet()));
    }

    private boolean addToIndex(int entity, T value) {
        return index.computeIfAbsent(value, x -> new TreeSet<>()).add(entity);
    }

    private void removeFromIndex(int entity, T value) {
        if (value != null) {
            Set<Integer> entities = index.get(value);
            entities.remove(entity);
            if (entities.isEmpty()) {
                index.remove(value);
            }
        }
    }
}
