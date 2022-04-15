package com.etherblood.luna.network.api.serialization;

import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.destrostudios.gametools.network.shared.serializers.UuidSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.movement.Speed;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameEngineSerializerTest {

    @Test
    public void copyGame() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        kryo.register(GameEngine.class, new GameEngineSerializer());
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(Position.class, new RecordSerializer<>());
        kryo.register(Speed.class, new RecordSerializer<>());
        kryo.register(Vector2.class, new RecordSerializer<>());

        GameEngine game = GameRules.getDefault().createGame();
        EntityData data = game.getData();
        int a = data.createEntity();
        int b = data.createEntity();

        data.set(a, new Position(0, 1));
        data.set(b, new Position(110, 111));
        data.set(b, new Speed(110_111));

        GameEngine copy = kryo.copy(game);

        assertEquals(game.getFrame(), copy.getFrame());
        assertEquals(game.getStartEpochMillis(), copy.getStartEpochMillis());
        assertEquals(game.getRules().getId(), copy.getRules().getId());

        assertEquals(data.peekNextEntity(), copy.getData().peekNextEntity());
        assertEquals(data.get(a, Position.class), copy.getData().get(a, Position.class));
        assertEquals(data.get(b, Position.class), copy.getData().get(b, Position.class));
        assertEquals(data.get(b, Speed.class), copy.getData().get(b, Speed.class));
    }
}
