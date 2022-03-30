package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.scene.Node;
import java.util.List;

public class ModelWrapper {

    private final int entity;
    private final Node node;
    private final List<String> animations;
    private int animationIndex = 0;

    private final BitmapText nameText;

    public ModelWrapper(int entity, Node node, List<String> animations, BitmapText nameText) {
        this.entity = entity;
        this.node = node;
        this.animations = animations;
        this.nameText = nameText;
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

    public BitmapText getNameText() {
        return nameText;
    }
}
