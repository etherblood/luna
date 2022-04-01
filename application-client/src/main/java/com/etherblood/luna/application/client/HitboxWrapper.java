package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.scene.Spatial;

public class HitboxWrapper {

    private final int entity;
    private final Spatial node;

    public HitboxWrapper(int entity, Spatial node) {
        this.entity = entity;
        this.node = node;
    }

    public int getEntity() {
        return entity;
    }

    public Spatial getNode() {
        return node;
    }
}
