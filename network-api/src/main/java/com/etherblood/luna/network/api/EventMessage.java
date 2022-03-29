package com.etherblood.luna.network.api;

import java.util.Arrays;

public record EventMessage(
        long seq,
        long ack,
        EventMessagePart[] parts
) {
    @Override
    public String toString() {
        return "EventMessage{" +
                "seq=" + seq +
                ", ack=" + ack +
                ", parts=" + Arrays.toString(parts) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventMessage that)) {
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
        int result = Long.hashCode(seq);
        result = 31 * result + Long.hashCode(ack);
        result = 31 * result + Arrays.hashCode(parts);
        return result;
    }
}
