package com.etherblood.luna.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityDataImpl implements EntityData {

    private final Map<Class<?>, ComponentTable<?>> data;
    private int nextEntity;

    public EntityDataImpl(Set<Class<?>> types) {
        data = types.stream().collect(Collectors.toMap(t -> t, t -> new ComponentTable<>()));
        nextEntity = 1;
    }

    @Override
    public int createEntity() {
        return nextEntity++;
    }

    @Override
    public <T> T get(int entity, Class<T> type) {
        return table(type).get(entity);
    }

    @Override
    public <T> void set(int entity, T value) {
        table(type(value)).set(entity, value);
    }

    @Override
    public void remove(int entity, Class<?> type) {
        table(type).remove(entity);
    }

    @Override
    public List<Integer> list(Class<?> type) {
        return table(type).list();
    }

    @Override
    public <T> List<Integer> findByValue(T value) {
        return table(type(value)).findByValue(value);
    }

    @Override
    public Set<Class<?>> getRegisteredClasses() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> type(T value) {
        return (Class<T>) value.getClass();
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentTable<T> table(Class<T> type) {
        ComponentTable<T> table = (ComponentTable<T>) data.get(type);
        if (table == null) {
            throw new NullPointerException("There is no table for " + type);
        }
        return table;
    }
}
