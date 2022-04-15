package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.scene.Spatial;

public class ModelWrapper {

    private final int entity;
    private final Spatial node;

    public ModelWrapper(int entity, Spatial node) {
        this.entity = entity;
        this.node = node;
    }

    public void setAnimationProgress(String animation, float progress) {
        AnimationControl a = node.getFirstControl(AnimationControl.class);
        setAnimationTime(animation, progress * a.getAnimation(animation).getDuration());
    }

    public void setAnimationTime(String animation, float seconds) {
        AnimationControl a = node.getFirstControl(AnimationControl.class);
        a.play(animation);
        a.setPlaying(false);
        a.setTime(seconds);
    }

    public int getEntity() {
        return entity;
    }

    public Spatial getNode() {
        return node;
    }
}
