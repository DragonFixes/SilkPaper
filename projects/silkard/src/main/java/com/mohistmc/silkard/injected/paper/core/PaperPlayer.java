package com.mohistmc.silkard.injected.paper.core;

import net.kyori.adventure.util.TriState;

/**
 * Player state that Paper adds to {@link net.minecraft.world.entity.player.Player}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperPlayer}.
 */
public interface PaperPlayer {

    /**
     * Whether a player who may fly still takes fall damage; {@link TriState#NOT_SET} keeps the
     * vanilla behavior of no fall damage while flying is allowed.
     */
    default TriState paper$flyingFallDamage() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setFlyingFallDamage(TriState flyingFallDamage) {
        throw new IllegalStateException("Not implemented");
    }
}
