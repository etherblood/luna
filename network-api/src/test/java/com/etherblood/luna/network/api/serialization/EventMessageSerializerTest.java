package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventMessageSerializerTest {

    @Test
    public void copyEventMessage() {
        Kryo kryo = new Kryo();
        kryo.register(EventMessage.class, new EventMessageSerializer());
        kryo.register(GameEvent.class, new RecordSerializer<GameEvent>());
        kryo.register(PlayerInput.class, new RecordSerializer<PlayerInput>());

        EventMessage message = new EventMessage(15, 25, new EventMessagePart[]{
                new EventMessagePart(19, new GameEvent(null)),
                new EventMessagePart(20, new GameEvent(new PlayerInput(1, Direction.UP, ActorAction.IDLE)))
        });

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output out = new Output(stream);
        kryo.writeObject(out, message);
        out.flush();

        Input in = new Input(new ByteArrayInputStream(stream.toByteArray()));
        EventMessage copy = kryo.readObject(in, EventMessage.class);


        assertEquals(message, copy);

    }
}
