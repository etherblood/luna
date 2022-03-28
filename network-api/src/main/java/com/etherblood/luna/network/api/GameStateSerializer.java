package com.etherblood.luna.network.api;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameStateSerializer extends Serializer<GameEngine> {

    public void init(Kryo kryo) {
        kryo.register(GameEngine.class, this);
    }

    @Override
    public void write(Kryo kryo, Output output, GameEngine object) {
        output.writeString(object.getRules().getId());
        EntityData data = object.getData();
        Set<Class<?>> classes = data.getRegisteredClasses();
        Map<Integer, Set<Object>> entityMap = new HashMap<>();
        for (Class<?> type : classes) {
            for (int entity : data.list(type)) {
                entityMap.computeIfAbsent(entity, e -> new HashSet<>()).add(data.get(entity, type));
            }
        }
        kryo.writeObject(output, entityMap);
    }

    @Override
    public GameEngine read(Kryo kryo, Input input, Class<GameEngine> type) {
        GameRules rules = GameRules.get(input.readString());
        GameEngine engine = rules.createGame();
        EntityData data = engine.getData();
        Map<Integer, Set<Object>> entityMap = kryo.readObject(input, HashMap.class);
        for (Map.Entry<Integer, Set<Object>> entry : entityMap.entrySet()) {
            for (Object value : entry.getValue()) {
                data.set(entry.getKey(), value);
            }
        }
        return engine;
    }
}
