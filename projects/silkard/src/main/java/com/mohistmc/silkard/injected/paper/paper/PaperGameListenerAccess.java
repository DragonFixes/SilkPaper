package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import io.papermc.paper.connection.PaperPlayerGameConnection;

/**
 * Members Paper adds to {@link net.minecraft.server.network.ServerGamePacketListenerImpl}.
 */
public interface PaperGameListenerAccess {

    /** The Paper connection API view of this listener (Paper's {@code playerGameConnection} field). */
    default PaperPlayerGameConnection paper$playerGameConnection() {
        throw new ContextStateException("Not implemented");
    }
}
