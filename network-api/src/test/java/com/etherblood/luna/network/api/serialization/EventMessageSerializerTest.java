package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventMessageSerializerTest {

    @Test
    public void copyEventMessage() {
        Kryo kryo = new Kryo();
        kryo.register(EventMessage.class, new EventMessageSerializer());
        kryo.register(GameEvent.class, new RecordSerializer<GameEvent>());
        kryo.register(PlayerInput.class, new RecordSerializer<PlayerInput>());

        EventMessage message = new EventMessage(9, 15, 25, new EventMessagePart[]{
                new EventMessagePart(19, new GameEvent(null)),
                new EventMessagePart(20, new GameEvent(new PlayerInput(13, Direction.DOWN_LEFT, ActorAction.DASH)))
        });

        EventMessage copy = kryo.copy(message);
        assertEquals(message, copy);

    }
}
