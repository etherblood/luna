package com.etherblood.luna.engine.actions;

public record Attack1Cooldown(
        long endsFrame
) implements CooldownComponent {
}
