package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperConnectionAccess;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper's per-connection state: handshake details, client brand and plugin channels, and the legacy
 * login event bookkeeping.
 */
@Mixin(Connection.class)
public abstract class MixinPaperConnection implements PaperConnectionAccess {

    @Unique private int paper$protocolVersion = -1;
    @Unique private @Nullable InetSocketAddress paper$virtualHost;
    @Unique private @Nullable SocketAddress paper$haProxyAddress;
    @Unique private @Nullable ServerPlayer paper$savedPlayerForLegacyEvents;
    @Unique private boolean paper$handledLegacyLoginEvent;
    @Unique private @Nullable Optional<net.minecraft.network.chat.Component> paper$legacySavedLoginEventResultOverride;
    @Unique private volatile @Nullable String paper$clientBrand;
    @Unique private final Set<String> paper$pluginMessagerChannels = ConcurrentHashMap.newKeySet();

    @Override
    public int paper$protocolVersion() {
        return this.paper$protocolVersion;
    }

    @Override
    public void paper$protocolVersion(final int protocolVersion) {
        this.paper$protocolVersion = protocolVersion;
    }

    @Override
    public @Nullable InetSocketAddress paper$virtualHost() {
        return this.paper$virtualHost;
    }

    @Override
    public void paper$virtualHost(final @Nullable InetSocketAddress virtualHost) {
        this.paper$virtualHost = virtualHost;
    }

    @Override
    public @Nullable SocketAddress paper$haProxyAddress() {
        return this.paper$haProxyAddress;
    }

    @Override
    public void paper$haProxyAddress(final @Nullable SocketAddress address) {
        this.paper$haProxyAddress = address;
    }

    @Override
    public @Nullable ServerPlayer paper$savedPlayerForLegacyEvents() {
        return this.paper$savedPlayerForLegacyEvents;
    }

    @Override
    public void paper$savedPlayerForLegacyEvents(final @Nullable ServerPlayer player) {
        this.paper$savedPlayerForLegacyEvents = player;
    }

    @Override
    public boolean paper$handledLegacyLoginEvent() {
        return this.paper$handledLegacyLoginEvent;
    }

    @Override
    public void paper$handledLegacyLoginEvent(final boolean handled) {
        this.paper$handledLegacyLoginEvent = handled;
    }

    @Override
    public @Nullable Optional<net.minecraft.network.chat.Component> paper$legacySavedLoginEventResultOverride() {
        return this.paper$legacySavedLoginEventResultOverride;
    }

    @Override
    public void paper$legacySavedLoginEventResultOverride(final @Nullable Optional<net.minecraft.network.chat.Component> override) {
        this.paper$legacySavedLoginEventResultOverride = override;
    }

    @Override
    public @Nullable String paper$clientBrand() {
        return this.paper$clientBrand;
    }

    @Override
    public void paper$clientBrand(final @Nullable String brand) {
        this.paper$clientBrand = brand;
    }

    @Override
    public Set<String> paper$pluginMessagerChannels() {
        return this.paper$pluginMessagerChannels;
    }
}