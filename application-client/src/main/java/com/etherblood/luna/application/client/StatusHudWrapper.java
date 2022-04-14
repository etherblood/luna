package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.scene.Node;
import com.etherblood.luna.engine.MathUtil;
import org.joml.Vector3f;

public class StatusHudWrapper {
    private final int entity;
    private final Node node;
    private final BitmapText nameNode;
    private final BitmapText healthNode;

    public StatusHudWrapper(int entity, BitmapFont font) {
        this.entity = entity;
        nameNode = new BitmapText(font);
        healthNode = new BitmapText(font);

        node = new Node();
        node.add(nameNode);
        node.add(healthNode);
    }

    public void setName(String name) {
        if (name == null) {
            name = "Nameless";
        }
        nameNode.setText(name);
        nameNode.setLocalTranslation(new Vector3f(nameNode.getTextWidth() / -2f, 10, 0));
    }

    public void setHealth(Long milliHealth) {
        if (milliHealth == null) {
            healthNode.setText("");
        } else {
            String text = Long.toString(Math.max(0, MathUtil.ceilDiv(milliHealth, 1000)));
            healthNode.setText(text);
            healthNode.setLocalTranslation(new Vector3f(healthNode.getTextWidth() / -2f, 30, 0));
        }
    }

    public int getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }
}
