package com.etherblood.luna.network.api.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

public class RecordSerializer<T extends Record> extends Serializer<T> {

    public RecordSerializer() {
        super(false, true);
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        Class<?> type = object.getClass();
        RecordComponent[] recordComponents = type.getRecordComponents();
        try {
            for (RecordComponent component : recordComponents) {
                Method accessor = component.getAccessor();
                kryo.writeObjectOrNull(output, accessor.invoke(object), component.getType());
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        RecordComponent[] recordComponents = type.getRecordComponents();
        Object[] args = new Object[recordComponents.length];
        for (int i = 0; i < args.length; i++) {
            RecordComponent component = recordComponents[i];
            args[i] = kryo.readObjectOrNull(input, component.getType());
        }
        try {
            Constructor<T> constructor = type.getConstructor(Stream.of(recordComponents).map(RecordComponent::getType).toArray(Class[]::new));
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
