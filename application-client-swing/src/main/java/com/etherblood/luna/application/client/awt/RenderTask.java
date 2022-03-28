package com.etherblood.luna.application.client.awt;

import com.etherblood.luna.engine.Rectangle;
import java.awt.Color;
import java.util.List;

public record RenderTask(
        Rectangle camera,
        List<Rectangle> actors,
        Color background
) {
}
