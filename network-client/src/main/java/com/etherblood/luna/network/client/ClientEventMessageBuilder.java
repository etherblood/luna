package com.etherblood.luna.network.client;

import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ClientEventMessageBuilder<T> {

    private long seq = 0;
    private long ack = -1;
    private final Map<EventMessagePart<T>, Long> pendingQueue = new HashMap<>();


    public void enqueueAction(EventMessagePart<T> part) {
        pendingQueue.put(part, seq);
    }

    public void updateAck(EventMessage<T> message) {
        ack = Math.max(ack, message.seq());
        pendingQueue.values().removeIf(x -> x <= message.ack());
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
