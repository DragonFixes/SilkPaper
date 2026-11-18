package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperServerGamePacketListener;
import io.papermc.paper.connection.PaperPlayerGameConnection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** One {@link PaperPlayerGameConnection} per game-phase listener, created on first use. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinPaperServerGamePacketListener implements PaperServerGamePacketListener {

    @Unique private volatile PaperPlayerGameConnection paper$playerGameConnection;

    @Override
    public PaperPlayerGameConnection paper$playerGameConnection() {
        PaperPlayerGameConnection connection = this.paper$playerGameConnection;
        if (connection == null) {
            synchronized (this) {
                connection = this.paper$playerGameConnection;
                if (connection == null) {
                    connection = new PaperPlayerGameConnection((ServerGamePacketListenerImpl) (Object) this);
                    this.paper$playerGameConnection = connection;
                }
            }
        }
        return connection;
    }
}
