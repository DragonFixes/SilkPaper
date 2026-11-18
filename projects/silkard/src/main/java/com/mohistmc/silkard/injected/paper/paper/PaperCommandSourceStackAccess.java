package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import org.jspecify.annotations.Nullable;

/**
 * Members Paper and Purpur add to {@link net.minecraft.commands.CommandSourceStack}.
 */
public interface PaperCommandSourceStackAccess {

    /**
     * Vanilla permission check combined with a Bukkit permission node, as Paper's command
     * system uses for every Paper/Purpur command.
     */
    default boolean hasPermission(final Permission permission, final String bukkitPermission) {
        throw new ContextStateException("Not implemented");
    }

    /** Purpur: like {@link #hasPermission(Permission, String)}, sending the permission message on failure. */
    default boolean testPermission(final Permission permission, final String bukkitPermission) {
        throw new ContextStateException("Not implemented");
    }

    /** Whether entity selectors may be used without the selector permission (Paper API). */
    default boolean paper$bypassSelectorPermissions() {
        throw new ContextStateException("Not implemented");
    }

    default void paper$bypassSelectorPermissions(final boolean bypass) {
        throw new ContextStateException("Not implemented");
    }

    /** Sends a failure message, red-styled only when {@code withStyle} is true. */
    default void sendFailure(final Component message, final boolean withStyle) {
        throw new ContextStateException("Not implemented");
    }

    /** Purpur: sends a MiniMessage-formatted success message; {@code null} sends nothing. */
    default void sendSuccess(final @Nullable String message) {
        this.sendSuccess(message, false);
    }

    default void sendSuccess(final @Nullable String message, final boolean broadcastToOps) {
        if (message == null) {
            return;
        }
        this.sendSuccess(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message), broadcastToOps);
    }

    default void sendSuccess(final net.kyori.adventure.text.@Nullable Component message) {
        this.sendSuccess(message, false);
    }

    default void sendSuccess(final net.kyori.adventure.text.@Nullable Component message, final boolean broadcastToOps) {
        throw new ContextStateException("Not implemented");
    }
}
