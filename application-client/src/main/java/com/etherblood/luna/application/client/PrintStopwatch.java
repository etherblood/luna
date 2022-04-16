package com.etherblood.luna.application.client;

public class PrintStopwatch implements AutoCloseable {

    private final String label;

    public PrintStopwatch(String label) {
        this.label = label;
    }

    private final long startNanos = System.nanoTime();

    @Override
    public void close() {
        System.out.println(label + ": " + humanReadableNanos(System.nanoTime() - startNanos));
    }

    public static String humanReadableNanos(long nanos) {
        int count = 0;
        while (nanos >= 10000 && count < 3) {
            nanos /= 1000;
            count++;
        }
        if (count == 3) {
            return nanos + "s";
        }
        return nanos + ("num".charAt(count) + "") + "s";
    }
}
