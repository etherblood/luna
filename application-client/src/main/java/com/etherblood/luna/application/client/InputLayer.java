package com.etherblood.luna.application.client;

import com.destrostudios.icetea.core.input.CharacterEvent;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.MouseButtonEvent;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import org.joml.Vector2f;

public interface InputLayer {

    default int orderNumber() {
        return 0;
    }

    default boolean consumeKey(KeyEvent event) {
        return false;
    }

    default boolean consumeCharacter(CharacterEvent event) {
        return false;
    }

    default boolean consumeMouseButton(MouseButtonEvent event, Vector2f cursorPosition) {
        return false;
    }

    default boolean consumeMouseMove(MousePositionEvent event) {
        return false;
    }
}
