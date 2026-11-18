package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.resource.ResourcePackCallback;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper adds to {@link net.minecraft.server.network.ServerCommonPacketListenerImpl}.
 * Brand and plugin channels live on the {@link net.minecraft.network.Connection}, so they carry over
 * from the configuration listener to the game listener.
 */
public interface PaperCommonListenerAccess {

    /** Brand the client sent through the {@code minecraft:brand} channel, or {@code null} before it did. */
    default @Nullable String paper$clientBrand() {
        throw new ContextStateException("Not implemented");
    }

    /** Plugin channels the client registered through {@code minecraft:register}. */
    default Set<String> paper$pluginMessagerChannels() {
        throw new ContextStateException("Not implemented");
    }

    /** Adventure callbacks for resource packs sent through this listener, keyed by pack id. */
    default Map<UUID, ResourcePackCallback> paper$packCallbacks() {
        throw new ContextStateException("Not implemented");
    }

    /** Cookie requests of this listener's Paper connection, or {@code null} for listeners without one. */
    default @Nullable ReadablePlayerCookieConnectionImpl paper$cookieConnection() {
        throw new ContextStateException("Not implemented");
    }
}
