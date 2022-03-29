package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.scene.Node;
import java.util.List;

public class ModelWrapper {
    
    private final int entity;
    private final Node node;
    private final List<String> animations;
    private int animationIndex = 0;

    public ModelWrapper(int entity, Node node, List<String> animations) {
        this.entity = entity;
        this.node = node;
        this.animations = animations;
    }

    public void setAnimation(String animation) {
        int nextIndex = animations.indexOf(animation);
        if (animationIndex != nextIndex) {
            AnimationControl a = (AnimationControl) node.getControls().iterator().next();
            a.play(nextIndex);
        }
        animationIndex = nextIndex;
    }

    public int getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }
}
