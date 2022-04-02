package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Node;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VK10;

public class StatusHudWrapper {
    private final int entity;
    private final Node node;
    private final BitmapText nameNode;
    private final BitmapText healthNode;
    private final float MAGIC = 9f;
    private static final float SCALE_MAGIC = 0.005f;

    public StatusHudWrapper(int entity, BitmapFont font) {
        this.entity = entity;
        nameNode = new BitmapText(font);
        healthNode = new BitmapText(font);

        nameNode.scale(new Vector3f(SCALE_MAGIC, -SCALE_MAGIC, SCALE_MAGIC));
        nameNode.setRenderBucket(RenderBucketType.TRANSPARENT);
        nameNode.getMaterial().setCullMode(VK10.VK_CULL_MODE_NONE);
        nameNode.setShadowMode(ShadowMode.OFF);

        healthNode.scale(new Vector3f(SCALE_MAGIC, -SCALE_MAGIC, SCALE_MAGIC));
        healthNode.setRenderBucket(RenderBucketType.TRANSPARENT);
        healthNode.getMaterial().setCullMode(VK10.VK_CULL_MODE_NONE);
        healthNode.setShadowMode(ShadowMode.OFF);

        node = new Node();
        node.add(nameNode);
        node.add(healthNode);
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        nameNode.setText(name);
        nameNode.setLocalTranslation(new Vector3f(-SCALE_MAGIC * name.length() / 2f * MAGIC, 0, 0));
    }

    public void setHealth(Integer health) {
        if (health == null) {
            healthNode.setText("");
        } else {
            String text = Integer.toString(Math.max(0, health));
            healthNode.setText(text);
            healthNode.setLocalTranslation(new Vector3f(-SCALE_MAGIC * text.length() / 2f * MAGIC, -0.1f, 0));
        }
    }

    public int getEntity() {
        return entity;
    }

    public Node getNode() {
        return node;
    }

//    public BitmapText getNameNode() {
//        return nameNode;
//    }
//
//    public BitmapText getHealthNode() {
//        return healthNode;
//    }
}
