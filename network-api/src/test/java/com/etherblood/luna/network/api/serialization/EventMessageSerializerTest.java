package com.etherblood.luna.network.api.serialization;

import com.destrostudios.gametools.network.shared.serializers.EnumSerializer;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.destrostudios.gametools.network.shared.serializers.UuidSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.etherblood.luna.engine.Direction;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.PlayerJoined;
import com.etherblood.luna.engine.Vector2;
import com.etherblood.luna.engine.actions.data.ActionType;
import com.etherblood.luna.network.api.game.messages.EventMessage;
import com.etherblood.luna.network.api.game.messages.EventMessagePart;
import com.etherblood.luna.network.api.game.messages.serialization.EventMessageSerializer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventMessageSerializerTest {

    @Test
    public void copyEventMessage() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setCopyReferences(false);
        kryo.register(EventMessage.class, new EventMessageSerializer());
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(GameEvent.class, new RecordSerializer<>());
        kryo.register(PlayerJoined.class, new RecordSerializer<>());
        kryo.register(PlayerInput.class, new RecordSerializer<>());
        kryo.register(ActionType.class, new EnumSerializer(ActionType.class));
        kryo.register(Direction.class, new RecordSerializer<>());
        kryo.register(Vector2.class, new RecordSerializer<>());

        EventMessage message = new EventMessage(UUID.randomUUID(), 9, 15, 25, new EventMessagePart[]{
                new EventMessagePart(19, new GameEvent(null, null)),
                new EventMessagePart(20, new GameEvent(new PlayerInput(13, Direction.DOWN_LEFT, ActionType.DASH), null))
        });

        EventMessage copy = kryo.copy(message);
        assertEquals(message, copy);

    }
}
