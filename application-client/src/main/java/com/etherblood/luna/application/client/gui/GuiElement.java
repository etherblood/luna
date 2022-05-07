package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.scene.Node;

public interface GuiElement extends EventConsumer {

    Node node();

    default void onFocus(boolean focus) {
    }

    BoundingRectangle bounds();

    boolean isFocused();

}
