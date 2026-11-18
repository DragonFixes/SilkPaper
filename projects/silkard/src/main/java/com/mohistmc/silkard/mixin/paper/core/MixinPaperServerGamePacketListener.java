package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperServerGamePacketListener;
import io.papermc.paper.connection.PaperPlayerGameConnection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * One {@link PaperPlayerGameConnection} per game-phase listener, created on first use. The game
 * listener overrides {@code handleCustomPayload} without calling the common listener, so a brand
 * re-sent during play is recorded here.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinPaperServerGamePacketListener implements PaperServerGamePacketListener {

    @Unique private volatile PaperPlayerGameConnection paper$playerGameConnection;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void paper$recordBrand(net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof net.minecraft.network.protocol.common.custom.BrandPayload(String brand)) {
            ((ServerGamePacketListenerImpl) (Object) this).connection.paper$setClientBrand(brand);
        }
    }

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
