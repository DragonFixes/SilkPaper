package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperLoginListenerAccess;
import io.papermc.paper.connection.PaperPlayerLoginConnection;
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
 * Keeps the profile id from the client's hello packet and gives each login listener its Paper
 * {@link PaperPlayerLoginConnection}.
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinPaperServerLoginPacketListenerImpl implements PaperLoginListenerAccess {

    @Unique private @Nullable UUID paper$requestedUuid;
    @Unique private PaperPlayerLoginConnection paper$loginConnection;

    // Created after the constructor body, since the connection view reads the listener's connection field.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void paper$createConnection(final CallbackInfo ci) {
        this.paper$loginConnection = new PaperPlayerLoginConnection((ServerLoginPacketListenerImpl) (Object) this);
    }

    @Inject(method = "handleHello", at = @At("HEAD"))
    private void paper$recordRequestedUuid(final ServerboundHelloPacket packet, final CallbackInfo ci) {
        this.paper$requestedUuid = packet.profileId();
    }

    @Override
    public @Nullable UUID paper$requestedUuid() {
        return this.paper$requestedUuid;
    }

    @Override
    public PaperPlayerLoginConnection paper$loginConnection() {
        return this.paper$loginConnection;
    }
}
