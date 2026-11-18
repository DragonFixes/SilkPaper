package com.mohistmc.silkard.injected.paper.server;

import org.jspecify.annotations.Nullable;

/**
 * Paper's configurable vibration listener range, injected into {@code SculkSensorBlockEntity}.
 */
public interface PaperSculkSensorBlockEntity {

    /**
     * @return the listener range override, or {@code null} for the vanilla range
     */
    default @Nullable Integer silkard$rangeOverride() {
        throw new IllegalStateException("Not implemented");
    }

    default void silkard$setRangeOverride(@Nullable Integer range) {
        throw new IllegalStateException("Not implemented");
    }
}
