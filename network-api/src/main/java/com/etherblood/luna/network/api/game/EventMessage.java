package com.etherblood.luna.network.api.game;

import java.util.Arrays;
import java.util.UUID;

public record EventMessage(
        UUID gameId,
        long lockFrame,
        long seq,
        long ack,
        EventMessagePart[] parts
) {
    @Override
    public String toString() {
        return "EventMessage{" +
                "gameId=" + gameId +
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

        if (!gameId.equals(that.gameId)) {
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
        int result = gameId.hashCode();
        result = 31 * result + Long.hashCode(lockFrame);
        result = 31 * result + Long.hashCode(seq);
        result = 31 * result + Long.hashCode(ack);
        result = 31 * result + Arrays.hashCode(parts);
        return result;
    }
}
