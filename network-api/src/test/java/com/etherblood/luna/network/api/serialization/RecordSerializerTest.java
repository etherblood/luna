package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.engine.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordSerializerTest {

    @Test
    public void copyRectangle() {
        Kryo kryo = new Kryo();
        RecordSerializer<Rectangle> serializer = new RecordSerializer<>();

        Rectangle rectangle = new Rectangle(1, 2, 3, 4);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output out = new Output(stream);
        serializer.write(kryo, out, rectangle);
        out.flush();

        Input in = new Input(new ByteArrayInputStream(stream.toByteArray()));
        Rectangle copy = serializer.read(kryo, in, Rectangle.class);

        assertEquals(rectangle, copy);
    }
}
