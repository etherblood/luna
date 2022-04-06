package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionKey;
import java.util.EnumMap;

public record SkillSet(
        // map with clearly defined iteration order
        EnumMap<ActionKey, String> skillMap
) {
}
