package com.etherblood.luna.engine;

public class MathUtil {

    public static int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }
}
