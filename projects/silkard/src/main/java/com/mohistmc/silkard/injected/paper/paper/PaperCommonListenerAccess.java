package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper adds to {@link net.minecraft.server.network.ServerCommonPacketListenerImpl}. Brand, plugin
 * channels and resource pack callbacks live on the connection ({@code PaperNetworkConnection}).
 */
public interface PaperCommonListenerAccess {

    /** Cookie requests of this listener's Paper connection, or {@code null} for listeners without one. */
    default @Nullable ReadablePlayerCookieConnectionImpl paper$cookieConnection() {
        throw new ContextStateException("Not implemented");
    }
}
