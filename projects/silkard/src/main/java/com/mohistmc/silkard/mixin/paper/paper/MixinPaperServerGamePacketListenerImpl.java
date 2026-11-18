package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperGameListenerAccess;
import io.papermc.paper.connection.PaperPlayerGameConnection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives each game listener its Paper {@link PaperPlayerGameConnection}.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinPaperServerGamePacketListenerImpl implements PaperGameListenerAccess {

    @Unique private PaperPlayerGameConnection paper$playerGameConnection;

    // Created after the constructor body, since the connection view reads the listener's connection field.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void paper$createConnection(final CallbackInfo ci) {
        this.paper$playerGameConnection = new PaperPlayerGameConnection((ServerGamePacketListenerImpl) (Object) this);
    }

    @Override
    public PaperPlayerGameConnection paper$playerGameConnection() {
        return this.paper$playerGameConnection;
    }
}
