package com.mohistmc.silkard.injected.network;

import com.mohistmc.silkard.util.ContextStateException;
import java.net.SocketAddress;

public interface ContextConnection {

    default String silkard$hostname() {
        throw new ContextStateException("Not implemented");
    }

    default void silkard$setHostName(String hostName) {
        throw new ContextStateException("Not implemented");
    }

    /** The player UUID a BungeeCord-style proxy forwarded in the handshake, or null. */
    default java.util.UUID silkard$spoofedUUID() {
        throw new ContextStateException("Not implemented");
    }

    default void silkard$setSpoofedUUID(java.util.UUID uuid) {
        throw new ContextStateException("Not implemented");
    }

    /** The profile properties (skin textures) a BungeeCord-style proxy forwarded, or null. */
    default com.mojang.authlib.properties.Property[] silkard$spoofedProfile() {
        throw new ContextStateException("Not implemented");
    }

    default void silkard$setSpoofedProfile(com.mojang.authlib.properties.Property[] profile) {
        throw new ContextStateException("Not implemented");
    }

    default SocketAddress getRawAddress() {
        throw new ContextStateException("Not implemented");
    }
}
