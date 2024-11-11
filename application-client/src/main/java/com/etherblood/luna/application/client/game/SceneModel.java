package com.etherblood.luna.application.client.game;

import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.scene.Spatial;

public record SceneModel(int entity, Spatial node) {

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
}
