package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.paper.paper.TickTimes;
import com.mohistmc.silkard.util.ContextStateException;

/**
 * Members Leaf adds to {@link net.minecraft.server.level.ServerLevel}: per-level tick times.
 */
public interface PaperServerLevelAccess {

    /** Level tick durations over the last 100 ticks (5 seconds). */
    default TickTimes paper$tickTimes5s() {
        throw new ContextStateException("Not implemented");
    }

    /** Level tick durations over the last 200 ticks (10 seconds). */
    default TickTimes paper$tickTimes10s() {
        throw new ContextStateException("Not implemented");
    }

    /** Level tick durations over the last 1200 ticks (1 minute). */
    default TickTimes paper$tickTimes1m() {
        throw new ContextStateException("Not implemented");
    }
}
