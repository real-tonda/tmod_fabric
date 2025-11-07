package com.togun.tmod.util;

public final class TickMetrics {
    private static final int SAMPLE_SIZE = 100;
    private static final long[] samples = new long[SAMPLE_SIZE];
    private static int sampleCount = 0;
    private static int index = 0;
    private static long lastTickTimestamp = -1L;

    private TickMetrics() {
    }

    public static synchronized void recordTick() {
        long now = System.nanoTime();
        if (lastTickTimestamp != -1L) {
            long duration = now - lastTickTimestamp;
            samples[index] = duration;
            index = (index + 1) % SAMPLE_SIZE;
            if (sampleCount < SAMPLE_SIZE) {
                sampleCount++;
            }
        }
        lastTickTimestamp = now;
    }

    public static synchronized double getAverageTickTimeMs() {
        if (sampleCount == 0) {
            return 0.0D;
        }

        long total = 0L;
        for (int i = 0; i < sampleCount; i++) {
            total += samples[i];
        }

        double averageNs = total / (double) sampleCount;
        return averageNs / 1_000_000.0D;
    }
}


