package com.etherblood.luna.network.server;

import com.etherblood.luna.network.api.game.EventMessage;
import com.etherblood.luna.network.api.game.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerEventMessageBuilder {

    private final UUID gameId;
    private long lockFrame = -1;
    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public ServerEventMessageBuilder(UUID gameId) {
        this.gameId = Objects.requireNonNull(gameId);
    }

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    public synchronized boolean broadcast(EventMessagePart part) {
        if (part.frame() > lockFrame) {
            pendingQueue.putIfAbsent(part, seq);
            return true;
        }
        // drop part, it is likely a late duplicate
        return false;
    }

    public synchronized void lockFrame(long lockFrame) {
        if (lockFrame <= this.lockFrame) {
            throw new IllegalArgumentException();
        }
        this.lockFrame = lockFrame;
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
