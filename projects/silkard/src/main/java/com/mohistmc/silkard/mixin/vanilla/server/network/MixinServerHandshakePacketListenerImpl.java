package com.mohistmc.silkard.mixin.vanilla.server.network;

import com.mohistmc.silkard.injected.network.ContextConnection;
import com.mohistmc.silkard.injected.server.ContextMinecraftServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mgazul
 * @date 2026/10/30
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public class MixinServerHandshakePacketListenerImpl {

    @Unique
    private static final Map<InetAddress, Long> silkard$throttleTracker = new HashMap<>();

    @Unique
    private static int silkard$throttleCounter = 0;

    @Shadow
    @Final
    private Connection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    // CraftBukkit start - set hostname
    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void silkard$hostname(ClientIntentionPacket packet, CallbackInfo ci) {
        ((ContextConnection) (Object) this.connection).silkard$setHostName(packet.hostName() + ":" + packet.port());
    }

    // CraftBukkit start - connection throttle
    @Inject(method = "beginLogin", at = @At("HEAD"), cancellable = true)
    private void silkard$throttle(ClientIntentionPacket packet, boolean transfer, CallbackInfo ci) {
        try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = ((ContextMinecraftServer) (Object) this.server).silkard$server().getConnectionThrottle();
            InetAddress address = ((InetSocketAddress) this.connection.getRemoteAddress()).getAddress();

            synchronized (silkard$throttleTracker) {
                if (silkard$throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - silkard$throttleTracker.get(address) < connectionThrottle) {
                    silkard$throttleTracker.put(address, currentTime);
                    Component message = Component.literal("Connection throttled! Please wait before reconnecting.");
                    this.connection.send(new ClientboundLoginDisconnectPacket(message));
                    this.connection.disconnect(message);
                    ci.cancel();
                    return;
                }

                silkard$throttleTracker.put(address, currentTime);
                silkard$throttleCounter++;
                if (silkard$throttleCounter > 200) {
                    silkard$throttleCounter = 0;
                    silkard$throttleTracker.entrySet().removeIf(entry -> entry.getValue() > connectionThrottle);
                }
            }
        } catch (Throwable throwable) {
            org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", throwable);
        }
    }
    // CraftBukkit end

    @Unique
    private static final com.google.gson.Gson silkard$gson = new com.google.gson.Gson();

    @Unique
    private static final java.util.regex.Pattern silkard$HOST_PATTERN = java.util.regex.Pattern.compile("[0-9a-f\\.:]{0,45}");

    @Unique
    private static final boolean silkard$BYPASS_HOSTCHECK = Boolean.getBoolean("Paper.bypassHostCheck");

    /**
     * Spigot BungeeCord IP forwarding and Paper's PlayerHandshakeEvent. A BungeeCord-style proxy
     * appends {@code \0<player ip>\0<uuid>[\0<properties json>]} to the handshake host name; this
     * reads it once the login listener is installed, before the client's hello arrives.
     */
    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void silkard$proxyForwarding(ClientIntentionPacket packet, boolean transfer, CallbackInfo ci) {
        ContextConnection context = (ContextConnection) (Object) this.connection;
        java.net.SocketAddress socketAddress = this.connection.getRemoteAddress();
        int port = socketAddress instanceof InetSocketAddress inet ? inet.getPort() : 0;
        boolean proxyLogicEnabled = org.spigotmc.SpigotConfig.bungee;
        String hostName = packet.hostName();

        boolean handledByEvent = false;
        if (com.destroystokyo.paper.event.player.PlayerHandshakeEvent.getHandlerList().getRegisteredListeners().length != 0) {
            String hostnameOfRemote = socketAddress instanceof InetSocketAddress inet ? inet.getHostString() : InetAddress.getLoopbackAddress().getHostAddress();
            com.destroystokyo.paper.event.player.PlayerHandshakeEvent event = new com.destroystokyo.paper.event.player.PlayerHandshakeEvent(hostName, hostnameOfRemote, !proxyLogicEnabled);
            if (event.callEvent()) {
                if (event.isFailed()) {
                    silkard$refuse(io.papermc.paper.adventure.PaperAdventure.asVanilla(event.failMessage()), ci);
                    return;
                }
                if (event.getServerHostname() != null) {
                    hostName = event.getServerHostname();
                    context.silkard$setHostName(hostName);
                }
                if (event.getSocketAddressHostname() != null) {
                    this.connection.address = new InetSocketAddress(event.getSocketAddressHostname(), port);
                }
                context.silkard$setSpoofedUUID(event.getUniqueId());
                context.silkard$setSpoofedProfile(silkard$gson.fromJson(event.getPropertiesJson(), com.mojang.authlib.properties.Property[].class));
                handledByEvent = true;
            }
        }

        String[] split = hostName.split("\0");
        if (!handledByEvent && proxyLogicEnabled) {
            if ((split.length == 3 || split.length == 4) && (silkard$BYPASS_HOSTCHECK || silkard$HOST_PATTERN.matcher(split[1]).matches())) {
                context.silkard$setHostName(split[0]);
                this.connection.address = new InetSocketAddress(split[1], port);
                context.silkard$setSpoofedUUID(com.mojang.util.UndashedUuid.fromStringLenient(split[2]));
            } else {
                silkard$refuse(Component.literal("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!"), ci);
                return;
            }
            if (split.length == 4) {
                context.silkard$setSpoofedProfile(silkard$gson.fromJson(split[3], com.mojang.authlib.properties.Property[].class));
            }
        } else if (!handledByEvent && (split.length == 3 || split.length == 4) && silkard$HOST_PATTERN.matcher(split[1]).matches()) {
            silkard$refuse(Component.literal("Unknown data in login hostname, did you forget to enable BungeeCord in spigot.yml?"), ci);
        }
    }

    @Unique
    private void silkard$refuse(Component message, CallbackInfo ci) {
        this.connection.send(new ClientboundLoginDisconnectPacket(message));
        this.connection.disconnect(message);
        ci.cancel();
    }
}
