package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.paper.core.PluginMessagePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Encodes {@link PluginMessagePayload} as its channel id plus raw bytes, ahead of the codec lookup
 * by id. Every other payload, including Fabric API's and Fabric mods' own, keeps the normal path.
 */
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public abstract class MixinPaperCustomPayloadCodec {

    @Inject(method = "encode(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", at = @At("HEAD"), cancellable = true)
    private void paper$encodePluginMessage(FriendlyByteBuf output, CustomPacketPayload value, CallbackInfo ci) {
        if (value instanceof PluginMessagePayload pluginMessage) {
            pluginMessage.write(output);
            ci.cancel();
        }
    }
}
