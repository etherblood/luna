package com.etherblood.luna.engine.actions;

public record Attack2Cooldown(
        long endsFrame
) implements CooldownComponent {
}
