package com.etherblood.luna.engine;

public record Team(
        int id
) {
    public static final Team PLAYERS = new Team(1);
    public static final Team OPPONENTS = new Team(2);
}
