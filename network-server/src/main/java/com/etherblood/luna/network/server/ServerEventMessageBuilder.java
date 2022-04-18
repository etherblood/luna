package com.etherblood.luna.network.server;

import com.etherblood.luna.network.api.game.messages.EventMessage;
import com.etherblood.luna.network.api.game.messages.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerEventMessageBuilder {

    private final UUID spectateId;
    private long lockFrame = -1;
    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public ServerEventMessageBuilder(UUID spectateId) {
        this.spectateId = Objects.requireNonNull(spectateId);
    }

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    public synchronized void broadcast(EventMessagePart part) {
        if (part.frame() > lockFrame) {
            pendingQueue.putIfAbsent(part, seq);
        } else {
            // this should never happen, duplicate/late messages should be filtered by playback buffer
            throw new IllegalStateException("Failed to broadcast " + part + " because builder is already locked.");
        }
    }

    public synchronized void lockFrame(long lockFrame) {
        if (lockFrame <= this.lockFrame) {
            throw new IllegalArgumentException();
        }
        this.lockFrame = lockFrame;
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
