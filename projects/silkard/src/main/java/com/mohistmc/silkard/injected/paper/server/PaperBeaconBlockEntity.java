package com.mohistmc.silkard.injected.paper.server;

/**
 * Paper's configurable beacon effect range, injected into {@code BeaconBlockEntity}.
 */
public interface PaperBeaconBlockEntity {

    /**
     * @return the effect range in blocks; the vanilla range for the beacon's level unless overridden
     */
    default double getEffectRange() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Overrides the effect range; a negative value restores the vanilla range.
     */
    default void setEffectRange(double range) {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Restores the vanilla effect range for the beacon's level.
     */
    default void resetEffectRange() {
        throw new IllegalStateException("Not implemented");
    }
}
