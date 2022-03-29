package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import java.util.List;

public class GameEngineSerializer extends Serializer<GameEngine> {
    @Override
    public void write(Kryo kryo, Output output, GameEngine object) {
        output.writeString(object.getRules().getId());
        output.writeLong(object.getStartEpochMillis());
        output.writeLong(object.getFrame());
        EntityData data = object.getData();
        output.writeInt(data.peekNextEntity());

        for (Class<?> registeredClass : data.getRegisteredClasses()) {
            List<Integer> entities = data.list(registeredClass);
            output.writeInt(entities.size());
            for (int entity : entities) {
                output.writeInt(entity);
                Object component = data.get(entity, registeredClass);
                kryo.writeObject(output, component);
            }
        }
    }

    @Override
    public GameEngine read(Kryo kryo, Input input, Class type) {
        String ruleId = input.readString();
        GameRules rules = GameRules.get(ruleId);
        long startEpochMillis = input.readLong();
        long frame = input.readLong();
        int lastEntity = input.readInt();
        EntityData data = new EntityDataImpl(rules.getComponentTypes(), lastEntity);

        for (Class<?> registeredClass : data.getRegisteredClasses()) {
            int length = input.readInt();
            for (int i = 0; i < length; i++) {
                int entity = input.readInt();
                Object component = kryo.readObject(input, registeredClass);
                data.set(entity, component);
            }
        }
        return new GameEngine(rules, startEpochMillis, data, frame);
    }
}
