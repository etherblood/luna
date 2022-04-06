package com.etherblood.luna.engine;

public class MathUtil {

    public static long ceilDiv(long x, long y) {
        return -Math.floorDiv(-x, y);
    }
}
