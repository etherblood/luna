package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.luna.engine.GameEvent;
import com.etherblood.luna.network.api.EventMessage;
import com.etherblood.luna.network.api.EventMessagePart;

public class EventMessageSerializer extends CopySerializer<EventMessage> {
    @Override
    public void write(Kryo kryo, Output output, EventMessage object) {
        output.writeLong(object.seq());
        output.writeLong(object.ack());
        output.writeInt(object.parts().length);

        for (EventMessagePart part : object.parts()) {
            output.writeLong(part.frame());
            kryo.writeObject(output, part.event());
        }
    }

    @Override
    public EventMessage read(Kryo kryo, Input input, Class type) {
        long seq = input.readLong();
        long ack = input.readLong();
        int length = input.readInt();

        EventMessagePart[] parts = new EventMessagePart[length];
        for (int i = 0; i < length; i++) {
            parts[i] = new EventMessagePart(input.readLong(), kryo.readObject(input, GameEvent.class));
        }
        return new EventMessage(seq, ack, parts);
    }
}
