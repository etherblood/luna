package com.etherblood.luna.engine;

public record Damagebox(
        Circle shape,
        DamageTrigger trigger,
        long milliDamage
) {
}
