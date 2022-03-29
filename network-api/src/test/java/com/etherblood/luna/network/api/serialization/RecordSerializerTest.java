package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.engine.Rectangle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordSerializerTest {

    @Test
    public void copyRectangle() {
        Kryo kryo = new Kryo();
        kryo.register(Rectangle.class, new RecordSerializer<>());

        Rectangle rectangle = new Rectangle(1, 2, 3, 4);

        Rectangle copy = kryo.copy(rectangle);
        assertEquals(rectangle, copy);
    }
}
