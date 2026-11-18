package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperLoginListenerAccess;
import java.util.UUID;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the profile id from the client's hello packet (Paper login connection API).
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinPaperServerLoginPacketListenerImpl implements PaperLoginListenerAccess {

    @Unique private @Nullable UUID paper$requestedUuid;

    @Inject(method = "handleHello", at = @At("HEAD"))
    private void paper$recordRequestedUuid(final ServerboundHelloPacket packet, final CallbackInfo ci) {
        this.paper$requestedUuid = packet.profileId();
    }

    @Override
    public @Nullable UUID paper$requestedUuid() {
        return this.paper$requestedUuid;
    }
}
