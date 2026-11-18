package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper and Purpur add to {@link net.minecraft.server.level.ServerPlayer}.
 */
public interface PaperServerPlayerAccess {

    /** Purpur: whether the TPS boss bar is shown to this player (saved with the player). */
    default boolean tpsBar() {
        throw new ContextStateException("Not implemented");
    }

    default void tpsBar(final boolean tpsBar) {
        throw new ContextStateException("Not implemented");
    }

    /** Purpur: whether the compass boss bar is shown to this player (saved with the player). */
    default boolean compassBar() {
        throw new ContextStateException("Not implemented");
    }

    default void compassBar(final boolean compassBar) {
        throw new ContextStateException("Not implemented");
    }

    /** Purpur: whether the RAM boss bar is shown to this player (saved with the player). */
    default boolean ramBar() {
        throw new ContextStateException("Not implemented");
    }

    default void ramBar(final boolean ramBar) {
        throw new ContextStateException("Not implemented");
    }

    /** Paper: the Adventure display name, or {@code null} when only the legacy name is set. */
    default @Nullable Component paper$adventureDisplayName() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$adventureDisplayName(final @Nullable Component displayName) {
        throw new ContextStateException("Not implemented");
    }
}
