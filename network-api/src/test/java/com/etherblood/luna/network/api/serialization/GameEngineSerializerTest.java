package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.data.EntityData;
import com.etherblood.luna.engine.GameEngine;
import com.etherblood.luna.engine.GameRules;
import com.etherblood.luna.engine.Position;
import com.etherblood.luna.engine.Speed;
import com.etherblood.luna.engine.Vector2;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameEngineSerializerTest {

    @Test
    public void copyGame() {
        Kryo kryo = new Kryo();
        kryo.register(GameEngine.class, new GameEngineSerializer());
        kryo.register(Position.class, new RecordSerializer<>());
        kryo.register(Speed.class, new RecordSerializer<>());
        kryo.register(Vector2.class, new RecordSerializer<>());

        GameEngine game = GameRules.getDefault().createGame();
        EntityData data = game.getData();
        int a = data.createEntity();
        int b = data.createEntity();

        data.set(a, new Position(0, 1));
        data.set(b, new Position(110, 111));
        data.set(b, new Speed(110, 111));


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output out = new Output(stream);
        kryo.writeObject(out, game);
        out.flush();

        Input in = new Input(new ByteArrayInputStream(stream.toByteArray()));
        GameEngine copy = kryo.readObject(in, GameEngine.class);


        assertEquals(game.getFrame(), copy.getFrame());
        assertEquals(game.getStartEpochMillis(), copy.getStartEpochMillis());
        assertEquals(game.getRules().getId(), copy.getRules().getId());

        assertEquals(data.peekNextEntity(), copy.getData().peekNextEntity());
        assertEquals(data.get(a, Position.class), copy.getData().get(a, Position.class));
        assertEquals(data.get(b, Position.class), copy.getData().get(b, Position.class));
        assertEquals(data.get(b, Speed.class), copy.getData().get(b, Speed.class));
    }
}
