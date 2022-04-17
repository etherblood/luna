package com.etherblood.luna.network.api.chat;

public record ChatMessage(
        long senderId,
        String senderName,
        long serverEpochMillis,
        String message
) {
}
