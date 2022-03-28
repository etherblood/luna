package com.etherblood.luna.data;

import java.util.List;
import java.util.Set;

public interface EntityData {

    int createEntity();

    <T> T get(int entity, Class<T> type);

    <T> void set(int entity, T value); // type is value.getClass()

    void remove(int entity, Class<?> type);

    List<Integer> list(Class<?> type); // all entities which have a component of given type

    <T> List<Integer> findByValue(T value);

    default boolean has(int entity, Class<?> type) {
        return get(entity, type) != null;
    }

    Set<Class<?>> getRegisteredClasses();

}
