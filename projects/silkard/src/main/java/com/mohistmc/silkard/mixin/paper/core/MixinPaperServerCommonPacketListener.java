package com.mohistmc.silkard.mixin.paper.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the client brand and resource pack responses on the connection, and runs the Adventure
 * callback registered for the answered pack.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinPaperServerCommonPacketListener {

    @org.spongepowered.asm.mixin.Unique
    private Connection paper$connection() {
        return ((ServerCommonPacketListenerImpl) (Object) this).connection;
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void paper$recordBrand(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof BrandPayload(String brand)) {
            this.paper$connection().paper$setClientBrand(brand);
        }
    }

    // TAIL runs once, on the main thread: the first, network-thread call leaves through
    // ensureRunningOnSameThread before reaching it.
    @Inject(method = "handleResourcePackResponse", at = @At("TAIL"))
    private void paper$recordPackResponse(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        this.paper$connection().paper$setResourcePackStatus(PlayerResourcePackStatusEvent.Status.values()[packet.action().ordinal()]);
        ResourcePackCallback callback = packet.action().isTerminal()
            ? this.paper$connection().paper$packCallbacks().remove(packet.id())
            : this.paper$connection().paper$packCallbacks().get(packet.id());
        if (callback != null) {
            Audience audience = ((ServerCommonPacketListenerImpl) (Object) this).getCraftPlayer();
            callback.packEventReceived(packet.id(), ResourcePackStatus.valueOf(packet.action().name()), audience == null ? Audience.empty() : audience);
        }
    }
}
