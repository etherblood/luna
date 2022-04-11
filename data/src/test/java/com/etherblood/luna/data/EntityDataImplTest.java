package com.etherblood.luna.data;

import java.util.Set;

public class EntityDataImplTest extends AbstractEntityDataTest {

    @Override
    protected EntityDataImpl createEntityData(Set<Class<?>> components) {
        return new EntityDataImpl(components);
    }
}
