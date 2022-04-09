package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.scene.Node;

public class ModelWrapper {

    private final int entity;
    private final Node node;

    public ModelWrapper(int entity, Node node) {
        this.entity = entity;
        this.node = node;
    }

    public void setAnimationTime(float seconds) {
        AnimationControl a = node.getFirstControl(AnimationControl.class);
        a.setTime(seconds);
    }

    public void setAnimation(String animation) {
        AnimationControl a = node.getFirstControl(AnimationControl.class);
        a.play(animation);
        a.setPlaying(false);
    }

    public int getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }
}
