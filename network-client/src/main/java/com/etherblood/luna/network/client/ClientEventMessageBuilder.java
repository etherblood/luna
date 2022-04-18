package com.etherblood.luna.network.client;

import com.etherblood.luna.network.api.game.messages.EventMessage;
import com.etherblood.luna.network.api.game.messages.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClientEventMessageBuilder {

    private final UUID spectateId;
    private long lockFrame = -1;
    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public ClientEventMessageBuilder(UUID spectateId) {
        this.spectateId = Objects.requireNonNull(spectateId);
    }

    public synchronized void enqueueAction(EventMessagePart part) {
        if (part.frame() <= lockFrame) {
            throw new IllegalStateException("Tried to request input for frame " + part.frame() + " when " + lockFrame + " frame was already locked.");
        }
        pendingQueue.put(part, seq);
        lockFrame = part.frame();
    }

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    public synchronized EventMessage build() {
        EventMessage result = new EventMessage(spectateId, lockFrame, seq, ack, pendingQueue.keySet().stream()
                .sorted(Comparator.comparingLong(EventMessagePart::frame))
                .toArray(EventMessagePart[]::new));
        seq++;
        return result;
    }

    public UUID getSpectateId() {
        return spectateId;
    }
}
