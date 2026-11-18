package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import io.papermc.paper.connection.PaperPlayerConfigurationConnection;

/**
 * Members Paper adds to {@link net.minecraft.server.network.ServerConfigurationPacketListenerImpl}.
 */
public interface PaperConfigurationListenerAccess {

    /** The Paper connection API view of this listener (Paper's {@code paperConnection} field). */
    default PaperPlayerConfigurationConnection paper$paperConnection() {
        throw new ContextStateException("Not implemented");
    }
}
