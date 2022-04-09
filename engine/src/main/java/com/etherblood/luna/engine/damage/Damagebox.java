package com.etherblood.luna.engine.damage;

import com.etherblood.luna.engine.Circle;

public record Damagebox(
        Circle area,
        DamageTrigger trigger,
        long milliDamage,
        boolean targetAllies,
        boolean targetOther
) {
    public Damagebox(Circle area, DamageTrigger trigger, long milliDamage) {
        this(area, trigger, milliDamage, false, true);
    }
}
