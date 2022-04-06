package com.etherblood.luna.network.server;

import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ServerEventMessageBuilder {

    // TODO: also send latest locked frame index to clients

    private long lockFrame = -1;
    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    public synchronized void broadcast(EventMessagePart... parts) {
        for (EventMessagePart part : parts) {
            if (part.frame() > lockFrame) {
                pendingQueue.putIfAbsent(part, seq);
            } else {
                // drop part, it is likely a late duplicate
            }
        }
    }

    public synchronized void lockFrame(long lockFrame) {
        if (lockFrame <= this.lockFrame) {
            throw new IllegalArgumentException();
        }
        this.lockFrame = lockFrame;
    }

    public synchronized EventMessage build() {
        EventMessage result = new EventMessage(lockFrame, seq, ack, pendingQueue.keySet().stream()
                .sorted(Comparator.comparingLong(EventMessagePart::frame))
                .toArray(EventMessagePart[]::new));
        seq++;
//        System.out.println("parts: " + result.parts().length + ", seq: " + result.seq() + ", ack: " + result.ack());
        return result;
    }
}
