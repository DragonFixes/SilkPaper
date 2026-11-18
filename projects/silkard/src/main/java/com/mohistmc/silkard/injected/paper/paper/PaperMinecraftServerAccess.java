package com.mohistmc.silkard.injected.paper.paper;

import ca.spottedleaf.common.time.TickData;
import com.mohistmc.silkard.util.ContextStateException;

/**
 * Members Paper, Gale and Purpur add to {@link net.minecraft.server.MinecraftServer}.
 */
public interface PaperMinecraftServerAccess {

    /** Tick durations over the last 5 seconds (Paper tick times API, /mspt). */
    default TickData paper$tickTimes5s() {
        throw new ContextStateException("Not implemented");
    }

    /** Tick durations over the last 10 seconds. */
    default TickData paper$tickTimes10s() {
        throw new ContextStateException("Not implemented");
    }

    /** Tick durations over the last minute. */
    default TickData paper$tickTimes1m() {
        throw new ContextStateException("Not implemented");
    }

    /** Whether the server is currently ticking its levels. */
    default boolean paper$isIteratingOverLevels() {
        throw new ContextStateException("Not implemented");
    }

    /** TPS averaged over 5 seconds, 1, 5 and 15 minutes, in that order (Gale/Purpur). */
    default double[] getTPSIncluding5SecondsReadOnly() {
        throw new ContextStateException("Not implemented");
    }

    /** Runs {@code task} on the server thread. */
    default void scheduleOnMain(final Runnable task) {
        throw new ContextStateException("Not implemented");
    }
}
