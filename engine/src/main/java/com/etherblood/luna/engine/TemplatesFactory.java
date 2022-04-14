package com.etherblood.luna.engine;

import java.util.Set;

public interface TemplatesFactory {

    Set<String> templateKeys();

    void apply(GameEngine game, int entity, String templateKey);
}
