package com.mohistmc.silkard.mixin.vanilla.server.network;

import io.netty.channel.Channel;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The channel initializer of the TCP listener in {@code ServerConnectionListener#startTcpServerListener}.
 */
@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public abstract class MixinServerConnectionListener$1 {

    // Paper - Add Channel initialization listeners: plugins such as ViaVersion add their pipeline
    // handlers here once the vanilla handlers are in place.
    @Inject(method = "initChannel", at = @At("TAIL"))
    private void silkard$callChannelInitializeListeners(Channel channel, CallbackInfo ci) {
        ChannelInitializeListenerHolder.callListeners(channel);
    }
}
