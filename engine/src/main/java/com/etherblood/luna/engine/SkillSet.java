package com.etherblood.luna.engine;

import com.etherblood.luna.engine.actions.ActionKey;
import java.util.Map;

public record SkillSet(
        Map<ActionKey, String> skillMap
) {
}
