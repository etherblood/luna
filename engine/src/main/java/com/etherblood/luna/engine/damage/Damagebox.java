package com.etherblood.luna.engine.damage;

import com.etherblood.luna.engine.Circle;

public record Damagebox(
        Circle shape,
        DamageTrigger trigger,
        long milliDamage
) {
}
