package com.mohistmc.silkard.injected.paper.core;

/**
 * Player state that Paper and Purpur add to {@link net.minecraft.server.level.ServerPlayer}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperServerPlayer}.
 */
public interface PaperServerPlayer {

    /** Wall-clock time in milliseconds at which this player object was created. */
    default long paper$loginTime() {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur AFK flag. */
    default boolean paper$afk() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setAfk(boolean afk) {
        throw new IllegalStateException("Not implemented");
    }
}
