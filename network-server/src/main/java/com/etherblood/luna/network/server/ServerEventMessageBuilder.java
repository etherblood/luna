package com.etherblood.luna.network.server;

import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ServerEventMessageBuilder {

    // TODO: also send latest locked frame index to clients

    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart, Long> pendingQueue = new HashMap<>();

    public void ackAndBroadcast(EventMessage message) {
        updateAck(message);
        broadcast(message.parts());
    }

    public synchronized void updateAck(EventMessage message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    @SafeVarargs
    public synchronized final void broadcast(EventMessagePart... parts) {
        for (EventMessagePart part : parts) {
            pendingQueue.putIfAbsent(part, seq);
        }
    }

    public synchronized EventMessage build() {
        EventMessage result = new EventMessage(seq, ack, pendingQueue.keySet().stream()
                .sorted(Comparator.comparingLong(EventMessagePart::frame))
                .toArray(EventMessagePart[]::new));
        seq++;
//        System.out.println("parts: " + result.parts().length + ", seq: " + result.seq() + ", ack: " + result.ack());
        return result;
    }
}
