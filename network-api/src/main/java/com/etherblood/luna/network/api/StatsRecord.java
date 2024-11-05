package com.etherblood.luna.network.api;

public record StatsRecord(
        long count,
        long minBytes,
        long maxBytes,
        long totalBytes
) {

    public long averageBytes() {
        return totalBytes / count;
    }
}
