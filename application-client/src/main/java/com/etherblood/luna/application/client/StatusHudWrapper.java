package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.scene.Node;
import org.joml.Vector3f;

public class StatusHudWrapper {
    private final int entity;
    private final Node node;
    private final BitmapText nameNode;
    private final BitmapText healthNode;
    private final float MAGIC = 9f;

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
            name = "";
        }
        nameNode.setText(name);
        nameNode.setLocalTranslation(new Vector3f(-name.length() / 2f * MAGIC, 10, 0));
    }

    public void setHealth(Integer health) {
        if (health == null) {
            healthNode.setText("");
        } else {
            String text = Integer.toString(Math.max(0, health));
            healthNode.setText(text);
            healthNode.setLocalTranslation(new Vector3f(-text.length() / 2f * MAGIC, 30, 0));
        }
    }

    public int getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }

}
