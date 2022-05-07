package com.etherblood.luna.application.client.gui;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import org.joml.Vector2f;

public interface EventConsumer {

    default boolean consumeKey(KeyEvent event) {
        return false;
    }

    default boolean consumeCharacter(CharacterEvent event, String character) {
        return false;
    }

    default boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        return false;
    }

    default boolean consumeMouseMove(MousePositionEvent event, Vector2f cursorPosition) {
        return false;
    }

}
