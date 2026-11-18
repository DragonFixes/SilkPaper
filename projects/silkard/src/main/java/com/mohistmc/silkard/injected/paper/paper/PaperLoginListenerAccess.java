package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import io.papermc.paper.connection.PaperPlayerLoginConnection;
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

    /** The Paper connection API view of this listener (Paper's {@code paperLoginConnection} field). */
    default PaperPlayerLoginConnection paper$loginConnection() {
        throw new ContextStateException("Not implemented");
    }
}