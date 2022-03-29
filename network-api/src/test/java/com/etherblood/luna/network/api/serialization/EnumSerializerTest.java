package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.engine.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumSerializerTest {

    @Test
    public void copyDirection() {
        Kryo kryo = new Kryo();
        kryo.register(Direction.class, new EnumSerializer(Direction.class));

        Direction direction = Direction.DOWN_RIGHT;

        Direction copy = kryo.copy(direction);
        assertEquals(direction, copy);
    }
}
