package com.mohistmc.silkard.paper.paper;

/**
 * A ring buffer of tick durations in nanoseconds (Paper's {@code MinecraftServer.TickTimes}).
 */
public final class TickTimes {

    private final long[] times;

    public TickTimes(final int length) {
        this.times = new long[length];
    }

    public void add(final int index, final long time) {
        this.times[index % this.times.length] = time;
    }

    public long[] getTimes() {
        return this.times.clone();
    }

    /** Average tick duration in milliseconds. */
    public double getAverage() {
        long total = 0L;
        for (final long value : this.times) {
            total += value;
        }
        return ((double) total / (double) this.times.length) * 1.0E-6D;
    }
}
