package com.mohistmc.silkard.injected.paper.core;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.resource.ResourcePackCallback;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jspecify.annotations.Nullable;

/**
 * Per-connection state Paper keeps for plugin messaging, the client brand and resource packs.
 * It lives on {@link net.minecraft.network.Connection} so it survives the switch from the
 * configuration listener to the game listener. Implemented by
 * {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperNetworkConnection}.
 */
public interface PaperNetworkConnection {

    /** Plugin channels the client registered; thread safe. */
    default Set<String> paper$pluginMessagerChannels() {
        throw new IllegalStateException("Not implemented");
    }

    /** Brand the client reported through {@code minecraft:brand}, or {@code null} before it did. */
    default @Nullable String paper$clientBrand() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setClientBrand(@Nullable String brand) {
        throw new IllegalStateException("Not implemented");
    }

    /** Adventure callbacks for resource packs sent to this connection, by pack id; thread safe. */
    default Map<UUID, ResourcePackCallback> paper$packCallbacks() {
        throw new IllegalStateException("Not implemented");
    }

    /** Last resource pack status the client reported, or {@code null} before any. */
    default PlayerResourcePackStatusEvent.@Nullable Status paper$resourcePackStatus() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setResourcePackStatus(PlayerResourcePackStatusEvent.@Nullable Status status) {
        throw new IllegalStateException("Not implemented");
    }
}
