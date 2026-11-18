package com.mohistmc.silkard.injected.paper.server;

/**
 * Paper's cook speed multiplier, injected into {@code AbstractFurnaceBlockEntity}.
 */
public interface PaperAbstractFurnaceBlockEntity {

    /**
     * @return the multiplier applied to recipe cook times; {@code 1.0} is vanilla speed
     */
    default double silkard$cookSpeedMultiplier() {
        throw new IllegalStateException("Not implemented");
    }

    default void silkard$setCookSpeedMultiplier(double multiplier) {
        throw new IllegalStateException("Not implemented");
    }
}
