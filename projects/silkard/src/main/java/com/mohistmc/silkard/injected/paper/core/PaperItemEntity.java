package com.mohistmc.silkard.injected.paper.core;

import net.kyori.adventure.util.TriState;

/**
 * Item entity state that Paper and Purpur add to {@link net.minecraft.world.entity.item.ItemEntity}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperItemEntity}.
 */
public interface PaperItemEntity {

    /** Whether mobs may pick this item up (Paper). */
    default boolean paper$canMobPickup() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setCanMobPickup(boolean canMobPickup) {
        throw new IllegalStateException("Not implemented");
    }

    /** Friction override; {@link TriState#NOT_SET} keeps vanilla friction. */
    default TriState paper$frictionState() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setFrictionState(TriState state) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur: immune to cactus damage. */
    default boolean paper$immuneToCactus() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setImmuneToCactus(boolean immune) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur: immune to explosion damage. */
    default boolean paper$immuneToExplosion() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setImmuneToExplosion(boolean immune) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur: immune to fire and lava. */
    default boolean paper$immuneToFire() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setImmuneToFire(boolean immune) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur: immune to lightning damage. */
    default boolean paper$immuneToLightning() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setImmuneToLightning(boolean immune) {
        throw new IllegalStateException("Not implemented");
    }
}
