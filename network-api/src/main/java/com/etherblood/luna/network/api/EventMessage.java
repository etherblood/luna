package com.etherblood.luna.network.api;

public record EventMessage<T>(
        long seq,
        long ack,
        EventMessagePart<T>[] parts
) {
}
