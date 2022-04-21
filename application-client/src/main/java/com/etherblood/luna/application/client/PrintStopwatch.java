package com.etherblood.luna.application.client;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class PrintStopwatch implements AutoCloseable {

    private final String label;

    public PrintStopwatch(String label) {
        this.label = label;
    }

    private final long startNanos = System.nanoTime();

    @Override
    public void close() {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println(label + ": " + humanReadableNanos(System.nanoTime() - startNanos));
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
        return nanos + ("nµm".charAt(count) + "") + "s";
    }
}
