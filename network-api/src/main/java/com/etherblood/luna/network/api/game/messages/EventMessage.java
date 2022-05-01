package com.etherblood.luna.network.api.game.messages;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record EventMessage(
        UUID spectateId,
        long lockFrame,
        long seq,
        long ack,
        EventMessagePart[] parts
) {
    public EventMessage {
        Objects.requireNonNull(spectateId);
        Objects.requireNonNull(parts);
        for (EventMessagePart part : parts) {
            Objects.requireNonNull(part);
        }
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "spectateId=" + spectateId +
                ", lockFrame=" + lockFrame +
                ", seq=" + seq +
                ", ack=" + ack +
                ", parts=" + Arrays.toString(parts) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventMessage)) {
            return false;
        }

        EventMessage that = (EventMessage) o;

        if (!spectateId.equals(that.spectateId)) {
            return false;
        }
        if (lockFrame != that.lockFrame) {
            return false;
        }
        if (seq != that.seq) {
            return false;
        }
        if (ack != that.ack) {
            return false;
        }
        return Arrays.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        int result = spectateId.hashCode();
        result = 31 * result + Long.hashCode(lockFrame);
        result = 31 * result + Long.hashCode(seq);
        result = 31 * result + Long.hashCode(ack);
        result = 31 * result + Arrays.hashCode(parts);
        return result;
    }
}
