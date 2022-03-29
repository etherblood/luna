package com.etherblood.luna.data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class EntityDataImpl implements EntityData {

    private final Map<Class<?>, ComponentTable<?>> data;
    private int nextEntity;

    public EntityDataImpl(Set<Class<?>> types) {
        this(types, 1);
    }

    public EntityDataImpl(Set<Class<?>> types, int nextEntity) {
        data = types.stream().collect(Collectors.toMap(t -> t, t -> new ComponentTable<>()));
        this.nextEntity = nextEntity;
    }

    @Override
    public int createEntity() {
        return nextEntity++;
    }

    @Override
    public int peekNextEntity() {
        return nextEntity;
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
        return data.keySet().stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Class::getName))));
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
