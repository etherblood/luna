package com.etherblood.luna.network.client;

import com.etherblood.luna.network.api.game.EventMessage;
import com.etherblood.luna.network.api.game.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClientEventMessageBuilder {

    private final UUID gameId;
    private long lockFrame = -1;
    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public ClientEventMessageBuilder(UUID gameId) {
        this.gameId = Objects.requireNonNull(gameId);
    }


    public synchronized void enqueueAction(EventMessagePart part) {
        if (part.frame() <= lockFrame) {
            throw new IllegalStateException("Game " + gameId + ": Tried to request input for frame " + part.frame() + " when " + lockFrame + " frame was already locked.");
        }
        pendingQueue.put(part, seq);
        lockFrame = part.frame();
    }

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    public synchronized EventMessage build() {
        EventMessage result = new EventMessage(gameId, lockFrame, seq, ack, pendingQueue.keySet().stream()
                .sorted(Comparator.comparingLong(EventMessagePart::frame))
                .toArray(EventMessagePart[]::new));
        seq++;
//        System.out.println("parts: " + result.parts().length + ", seq: " + result.seq() + ", ack: " + result.ack());
        return result;
    }
}
