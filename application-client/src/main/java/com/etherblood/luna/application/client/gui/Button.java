package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Button {

    private final Node node = new Node();
    private final Geometry background;
    private final BitmapText bitmapText;
    private Vector4f hitbox = new Vector4f(0, 0, 100, 100);

    public Button(Geometry background, BitmapText bitmapText) {
        this.background = background;
        this.bitmapText = bitmapText;
        node.add(background);
        node.add(bitmapText);
    }

    public void setDimensions(Vector2f position, Vector2f size) {
        hitbox = new Vector4f(position.x, position.y, position.x + size.x, position.y + size.y);
        update();
    }

    public void setText(String text) {
        if (text.isBlank()) {
            text = " ";
        }
        bitmapText.setText(text);
        update();
    }

    private void update() {
        Vector2f position = new Vector2f(hitbox.x, hitbox.y);
        Vector2f size = new Vector2f(hitbox.z - hitbox.x, hitbox.w - hitbox.y);
        background.setLocalTranslation(new Vector3f(position.x, position.y, 0));
        background.setLocalScale(new Vector3f(size.x, size.y, 1));
        bitmapText.setLocalTranslation(new Vector3f(
                position.x + (size.x - bitmapText.getTextWidth()) / 2,
                position.y + (size.y - bitmapText.getTextHeight()) / 2,
                0.001f));
    }

    public boolean contains(Vector2f position) {
        return hitbox.x <= position.x && position.x < hitbox.z
                && hitbox.y <= position.y && position.y < hitbox.w;
    }

    public Node getNode() {
        return node;
    }
}
