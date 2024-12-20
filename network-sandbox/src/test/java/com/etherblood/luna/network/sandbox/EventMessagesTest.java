package com.etherblood.luna.network.sandbox;

import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.engine.actions.data.ActionType;
import com.etherblood.luna.network.api.game.PlaybackBuffer;
import com.etherblood.luna.network.api.game.messages.EventMessage;
import com.etherblood.luna.network.api.game.messages.EventMessagePart;
import com.etherblood.luna.network.client.ClientEventMessageBuilder;
import com.etherblood.luna.network.server.ServerEventMessageBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventMessagesTest {

    @Test
    public void simpleRequest() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request = client.build();

        server.updateAck(request);
        for (EventMessagePart part : request.parts()) {
            server.broadcast(part);
        }

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void duplicatedRequest() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request = client.build();

        server.updateAck(request);
        for (EventMessagePart part : request.parts()) {
            server.broadcast(part);
        }
        server.updateAck(request);
        for (EventMessagePart part : request.parts()) {
            server.broadcast(part);
        }

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void droppedRequest_withFollowUpInput() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActionType.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();// dropped request
        client.enqueueAction(input1);
        EventMessage request2 = client.build();

        server.updateAck(request2);
        for (EventMessagePart part : request2.parts()) {
            server.broadcast(part);
        }

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0, input1}, response.parts());
    }

    @Test
    public void droppedRequest_withoutFollowUpInput() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();// dropped request
        EventMessage request2 = client.build();

        server.updateAck(request2);
        for (EventMessagePart part : request2.parts()) {
            server.broadcast(part);
        }

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void outOfOrderRequest() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActionType.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();
        client.enqueueAction(input1);
        EventMessage request2 = client.build();

        server.updateAck(request2);
        for (EventMessagePart part : request2.parts()) {
            server.broadcast(part);
        }
        server.updateAck(request1);
        for (EventMessagePart part : request1.parts()) {
            server.broadcast(part);
        }

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0, input1}, response.parts());
    }

    @Test
    public void simpleUpdate() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        PlaybackBuffer clientBuffer = new PlaybackBuffer();

        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));

        // when
        EventMessage message = new EventMessage(spectateId, 0, 0, -1, new EventMessagePart[]{input0});
        server.updateAck(message);
        for (EventMessagePart part : message.parts()) {
            server.broadcast(part);
        }
        EventMessage update = server.build();
        ackAndBuffer(client, clientBuffer, update);

        // then
        assertEquals(Set.of(input0.event()), clientBuffer.peek(0));
    }

    @Test
    public void duplicatedUpdate() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        PlaybackBuffer clientBuffer = new PlaybackBuffer();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));

        // when
        EventMessage message = new EventMessage(spectateId, 0, 0, -1, new EventMessagePart[]{input0});
        server.updateAck(message);
        for (EventMessagePart part : message.parts()) {
            server.broadcast(part);
        }
        EventMessage update = server.build();
        ackAndBuffer(client, clientBuffer, update);
        ackAndBuffer(client, clientBuffer, update);

        // then
        assertEquals(Set.of(input0.event()), clientBuffer.peek(0));
    }

    @Test
    public void outOfOrderUpdate() {
        // given
        UUID spectateId = UUID.randomUUID();
        ServerEventMessageBuilder server = new ServerEventMessageBuilder(spectateId);
        ClientEventMessageBuilder client = new ClientEventMessageBuilder(spectateId);
        PlaybackBuffer clientBuffer = new PlaybackBuffer();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActionType.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActionType.IDLE), null));

        // when
        EventMessage message1 = new EventMessage(spectateId, 0, 0, -1, new EventMessagePart[]{input0});
        server.updateAck(message1);
        for (EventMessagePart part : message1.parts()) {
            server.broadcast(part);
        }
        EventMessage update0 = server.build();
        EventMessage message = new EventMessage(spectateId, 0, 1, -1, new EventMessagePart[]{input1});
        server.updateAck(message);
        for (EventMessagePart part : message.parts()) {
            server.broadcast(part);
        }
        EventMessage update1 = server.build();
        ackAndBuffer(client, clientBuffer, update1);
        ackAndBuffer(client, clientBuffer, update0);

        // then
        assertEquals(Set.of(input0.event()), clientBuffer.peek(0));
        assertEquals(Set.of(input1.event()), clientBuffer.peek(1));
    }

    private void ackAndBuffer(ClientEventMessageBuilder client, PlaybackBuffer clientBuffer, EventMessage update) {
        client.updateAck(update);
        for (EventMessagePart part : update.parts()) {
            clientBuffer.buffer(part.frame(), part.event());
        }
    }
}
