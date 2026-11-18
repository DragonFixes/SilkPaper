package com.mohistmc.silkard.mixin.paper.paper;

import java.net.InetSocketAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the handshake's protocol version and virtual host on the connection (Paper network API).
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class MixinPaperServerHandshakePacketListenerImpl {

    @Shadow @Final private Connection connection;

    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void paper$recordHandshake(final ClientIntentionPacket packet, final CallbackInfo ci) {
        this.connection.paper$protocolVersion(packet.protocolVersion());
        // Forwarding proxies append data after a NUL byte; the virtual host is only the host name.
        String host = packet.hostName();
        final int nul = host.indexOf('\0');
        if (nul >= 0) {
            host = host.substring(0, nul);
        }
        this.connection.paper$virtualHost(InetSocketAddress.createUnresolved(host, packet.port()));
    }
}
