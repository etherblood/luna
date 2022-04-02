package com.etherblood.luna.network.sandbox;

import com.etherblood.luna.engine.ActorAction;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.engine.PlayerInput;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import com.etherblood.luna.network.api.PlaybackBuffer;
import com.etherblood.luna.network.client.ClientEventMessageBuilder;
import com.etherblood.luna.network.server.ServerEventMessageBuilder;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventMessagesTest {

    @Test
    public void simpleRequest() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request = client.build();

        server.ackAndBroadcast(request);

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void duplicatedRequest() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request = client.build();

        server.ackAndBroadcast(request);
        server.ackAndBroadcast(request);

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void droppedRequest_withFollowUpInput() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActorAction.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();// dropped request
        client.enqueueAction(input1);
        EventMessage request2 = client.build();

        server.ackAndBroadcast(request2);

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0, input1}, response.parts());
    }

    @Test
    public void droppedRequest_withoutFollowUpInput() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();// dropped request
        EventMessage request2 = client.build();

        server.ackAndBroadcast(request2);

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0}, response.parts());
    }

    @Test
    public void outOfOrderRequest() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActorAction.IDLE), null));

        // when
        client.enqueueAction(input0);
        EventMessage request1 = client.build();
        client.enqueueAction(input1);
        EventMessage request2 = client.build();

        server.ackAndBroadcast(request2);
        server.ackAndBroadcast(request1);

        // then
        EventMessage response = server.build();
        assertArrayEquals(new EventMessagePart[]{input0, input1}, response.parts());
    }

    @Test
    public void simpleUpdate() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        PlaybackBuffer clientBuffer = new PlaybackBuffer();

        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));

        // when
        server.ackAndBroadcast(new EventMessage(0, 0, -1, new EventMessagePart[]{input0}));
        EventMessage update = server.build();
        ackAndBuffer(client, clientBuffer, update);

        // then
        assertEquals(Set.of(input0.event()), clientBuffer.peek(0));
    }

    @Test
    public void duplicatedUpdate() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        PlaybackBuffer clientBuffer = new PlaybackBuffer();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));

        // when
        server.ackAndBroadcast(new EventMessage(0, 0, -1, new EventMessagePart[]{input0}));
        EventMessage update = server.build();
        ackAndBuffer(client, clientBuffer, update);
        ackAndBuffer(client, clientBuffer, update);

        // then
        assertEquals(Set.of(input0.event()), clientBuffer.peek(0));
    }

    @Test
    public void outOfOrderUpdate() {
        // given
        ServerEventMessageBuilder server = new ServerEventMessageBuilder();
        ClientEventMessageBuilder client = new ClientEventMessageBuilder();
        PlaybackBuffer clientBuffer = new PlaybackBuffer();
        EventMessagePart input0 = new EventMessagePart(0, new GameEvent(new PlayerInput(0, null, ActorAction.IDLE), null));
        EventMessagePart input1 = new EventMessagePart(1, new GameEvent(new PlayerInput(1, null, ActorAction.IDLE), null));

        // when
        server.ackAndBroadcast(new EventMessage(0, 0, -1, new EventMessagePart[]{input0}));
        EventMessage update0 = server.build();
        server.ackAndBroadcast(new EventMessage(0, 1, -1, new EventMessagePart[]{input1}));
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
