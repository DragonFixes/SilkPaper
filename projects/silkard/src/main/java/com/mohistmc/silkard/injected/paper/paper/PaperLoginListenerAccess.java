package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper adds to {@link net.minecraft.server.network.ServerLoginPacketListenerImpl}.
 */
public interface PaperLoginListenerAccess {

    /** Profile id the client sent in its hello packet. */
    default @Nullable UUID paper$requestedUuid() {
        throw new ContextStateException("Not implemented");
    }
}
