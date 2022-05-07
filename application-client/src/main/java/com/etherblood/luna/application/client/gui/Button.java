package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Button extends BaseGuiElement {

    private final BitmapText bitmapText;
    private final Runnable confirm;

    public Button(Spatial background, BoundingRectangle bounds, BitmapText bitmapText, Runnable confirm) {
        super(background, bounds);
        this.bitmapText = bitmapText;
        this.confirm = confirm;
        node().add(bitmapText);
        update();
    }

    @Override
    public void setBounds(BoundingRectangle bounds) {
        super.setBounds(bounds);
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
        Vector2f size = new Vector2f(bounds.width(), bounds.height());
        Vector3f translation = new Vector3f(
                (size.x - bitmapText.getTextWidth()) / 2,
                (size.y - bitmapText.getTextHeight()) / 2,
                BaseGuiElement.MAX_Z);
        if (!translation.equals(bitmapText.getLocalTransform().getTranslation(), 1e-6f)) {
            bitmapText.setLocalTranslation(translation);
        }
    }

    @Override
    public boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (event.getAction() == GLFW.GLFW_RELEASE && bounds().contains(cursorPosition)) {
                confirm.run();
            }
            return true;
        }
        return super.consumeMouseButton(event, cursorPosition);
    }
}
