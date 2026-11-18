package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper adds to {@link net.minecraft.network.Connection}.
 */
public interface PaperConnectionAccess {

    /** Protocol version the client sent in its handshake, or -1 before the handshake. */
    default int paper$protocolVersion() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$protocolVersion(final int protocolVersion) {
        throw new ContextStateException("Not implemented");
    }

    /** Host name and port the client connected to, as sent in its handshake. */
    default @Nullable InetSocketAddress paper$virtualHost() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$virtualHost(final @Nullable InetSocketAddress virtualHost) {
        throw new ContextStateException("Not implemented");
    }

    /** Address reported by an HAProxy PROXY header, or {@code null} without one. */
    default @Nullable SocketAddress paper$haProxyAddress() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$haProxyAddress(final @Nullable SocketAddress address) {
        throw new ContextStateException("Not implemented");
    }

    /** The player object handed to legacy (pre-configuration) login events. */
    default @Nullable ServerPlayer paper$savedPlayerForLegacyEvents() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$savedPlayerForLegacyEvents(final @Nullable ServerPlayer player) {
        throw new ContextStateException("Not implemented");
    }

    /** Whether the legacy {@code PlayerLoginEvent} already ran for this connection. */
    default boolean paper$handledLegacyLoginEvent() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$handledLegacyLoginEvent(final boolean handled) {
        throw new ContextStateException("Not implemented");
    }

    /** Kick message chosen by the legacy login event, applied once the player joins. */
    default @Nullable Optional<net.minecraft.network.chat.Component> paper$legacySavedLoginEventResultOverride() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$legacySavedLoginEventResultOverride(final @Nullable Optional<net.minecraft.network.chat.Component> override) {
        throw new ContextStateException("Not implemented");
    }

    /** Brand the client sent through the {@code minecraft:brand} channel, or {@code null} before it did. */
    default @Nullable String paper$clientBrand() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$clientBrand(final @Nullable String brand) {
        throw new ContextStateException("Not implemented");
    }

    /** Plugin channels the client registered; shared by every listener of this connection. */
    default Set<String> paper$pluginMessagerChannels() {
        throw new ContextStateException("Not implemented");
    }
}