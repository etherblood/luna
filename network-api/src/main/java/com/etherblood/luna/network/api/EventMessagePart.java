package com.etherblood.luna.network.api;

public record EventMessagePart<T>(
        long frame,
        T event
) {
}
