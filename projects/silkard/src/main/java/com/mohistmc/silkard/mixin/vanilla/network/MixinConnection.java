package com.mohistmc.silkard.mixin.vanilla.network;

import com.mohistmc.silkard.injected.network.ContextConnection;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mgazul
 * @date 2026/6/8
 */
@Mixin(Connection.class)
public abstract class MixinConnection implements ContextConnection {

    // CraftBukkit - add field (CB 自增字段，26.3 vanilla 无此成员，必须 @Unique 而非 @Shadow)
    @Unique
    public String hostname = "";

    @Shadow
    public abstract SocketAddress getRemoteAddress();

    // CraftBukkit - we can't wait here, this may be called from an event loop
    @Redirect(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;"))
    private ChannelFuture silkard$closeWithoutBlocking(ChannelFuture future) {
        return future;
    }

    @Override
    public String silkard$hostname() {
        return hostname;
    }

    @Override
    public void silkard$setHostName(String hostName) {
        this.hostname = hostName;
    }

    // Spigot - BungeeCord IP forwarding
    @Unique
    private volatile java.util.UUID silkard$spoofedUUID;

    @Unique
    private volatile com.mojang.authlib.properties.Property[] silkard$spoofedProfile;

    @Override
    public java.util.UUID silkard$spoofedUUID() {
        return this.silkard$spoofedUUID;
    }

    @Override
    public void silkard$setSpoofedUUID(java.util.UUID uuid) {
        this.silkard$spoofedUUID = uuid;
    }

    @Override
    public com.mojang.authlib.properties.Property[] silkard$spoofedProfile() {
        return this.silkard$spoofedProfile;
    }

    @Override
    public void silkard$setSpoofedProfile(com.mojang.authlib.properties.Property[] profile) {
        this.silkard$spoofedProfile = profile;
    }

    @Override
    public SocketAddress getRawAddress() {
        return getRemoteAddress();
    }
}
