package com.etherblood.luna.network.api.game.messages.serialization;

import com.destrostudios.gametools.network.shared.serializers.CopySerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.data.EntityDataImpl;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import java.util.List;
import java.util.UUID;

public class GameEngineSerializer extends CopySerializer<GameEngine> {
    @Override
    public void write(Kryo kryo, Output output, GameEngine object) {
        kryo.writeObject(output, object.getId());
        output.writeString(object.getRules().getId());
        output.writeLong(object.getStartEpochMillis());
        output.writeLong(object.getFrame());
        EntityData data = object.getData();
        output.writeInt(data.peekNextEntity());

        for (Class<?> component : data.getRegisteredClasses()) {
            List<Integer> entities = data.list(component);
            output.writeInt(entities.size());
            for (int entity : entities) {
                output.writeInt(entity);
                Object value = data.get(entity, component);
                kryo.writeObject(output, value);
            }
        }
    }

    @Override
    public GameEngine read(Kryo kryo, Input input, Class<GameEngine> type) {
        UUID gameId = kryo.readObject(input, UUID.class);
        String ruleId = input.readString();
        GameRules rules = GameRules.get(ruleId);
        long startEpochMillis = input.readLong();
        long frame = input.readLong();
        int nextEntity = input.readInt();
        EntityData data = new EntityDataImpl(rules.getComponentTypes(), nextEntity);

        for (Class<?> component : data.getRegisteredClasses()) {
            int length = input.readInt();
            for (int i = 0; i < length; i++) {
                int entity = input.readInt();
                Object value = kryo.readObject(input, component);
                data.set(entity, value);
            }
        }
        return new GameEngine(gameId, rules, startEpochMillis, data, frame);
    }
}
