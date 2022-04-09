package com.etherblood.luna.engine.actions.data;

public record ActionInterruptResist(
        long frames,
        int level
) {

    public static int NONE = 0;
    public static int LOW = 1;
    public static int MEDIUM = 2;
    public static int HIGH = 3;
    public static int MAX = 5;

}
