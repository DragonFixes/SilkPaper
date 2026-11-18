package com.mohistmc.silkard.injected.paper.core;

import io.papermc.paper.connection.PaperPlayerGameConnection;

/**
 * Paper's API view of a game-phase connection, added to
 * {@link net.minecraft.server.network.ServerGamePacketListenerImpl}. Implemented by
 * {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperServerGamePacketListener}.
 */
public interface PaperServerGamePacketListener {

    default PaperPlayerGameConnection paper$playerGameConnection() {
        throw new IllegalStateException("Not implemented");
    }
}
