package com.etherblood.luna.data;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractEntityDataTest {

    protected abstract EntityData createEntityData(Set<Class<?>> components);

    @Test
    public void set_get_remove() {
        EntityData data = createEntityData(Set.of(Integer.class, String.class));
        int entity = data.createEntity();
        String value = "testValue";

        assertNull(data.get(entity, String.class));
        data.set(entity, value);
        assertEquals(value, data.get(entity, String.class));
        data.remove(entity, String.class);
        assertNull(data.get(entity, String.class));
    }

}
