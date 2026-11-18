package com.mohistmc.silkard.injected.paper.server;

/**
 * Paper's per-slot "stop cooking" switches, injected into {@code CampfireBlockEntity}.
 */
public interface PaperCampfireBlockEntity {

    /**
     * @return the live per-slot flags; a {@code true} slot does not advance its cooking progress
     */
    default boolean[] silkard$stopCooking() {
        throw new IllegalStateException("Not implemented");
    }
}
