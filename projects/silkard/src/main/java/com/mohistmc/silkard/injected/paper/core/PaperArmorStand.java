package com.mohistmc.silkard.injected.paper.core;

/**
 * Armor stand switches that Paper adds to {@link net.minecraft.world.entity.decoration.ArmorStand}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperArmorStand}.
 */
public interface PaperArmorStand {

    /** Whether the armor stand may be moved by physics and pushes. */
    default boolean paper$canMove() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setCanMove(boolean canMove) {
        throw new IllegalStateException("Not implemented");
    }

    /** Whether the armor stand ticks at all. */
    default boolean paper$canTick() {
        throw new IllegalStateException("Not implemented");
    }

    /** Sets the tick switch as an API override, which is saved with the entity. */
    default void paper$setCanTick(boolean canTick) {
        throw new IllegalStateException("Not implemented");
    }
}
