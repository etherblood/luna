package com.etherblood.luna.network.server;

import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ServerEventMessageBuilder<T> {

    // TODO: also send latest locked frame index to clients

    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart<T>, Long> pendingQueue = new HashMap<>();

    public void ackAndBroadcast(EventMessage<T> message) {
        updateAck(message);
        broadcast(message.parts());
    }

    public void updateAck(EventMessage<T> message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
    }

    @SafeVarargs
    public final void broadcast(EventMessagePart<T>... parts) {
        for (EventMessagePart<T> part : parts) {
            pendingQueue.putIfAbsent(part, seq);
        }
    }

    public EventMessage<T> build() {
        EventMessage<T> result = new EventMessage<>(seq, ack, pendingQueue.keySet().stream()
                .sorted(Comparator.comparingLong(EventMessagePart::frame))
                .toArray(EventMessagePart[]::new));
        seq++;
//        System.out.println("parts: " + result.parts().length + ", seq: " + result.seq() + ", ack: " + result.ack());
        return result;
    }
}
