package com.mohistmc.silkard.paper.paper;

import com.google.common.base.Preconditions;
import io.papermc.paper.connection.PluginMessageBridgeImpl;
import java.util.Set;
import net.minecraft.network.Connection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jspecify.annotations.Nullable;

/**
 * Tracks the plugin channels a client registers through Silkard's custom payload handling. Outgoing
 * plugin messages are built with {@code com.mohistmc.silkard.paper.core.PluginMessagePayload}.
 */
public final class PluginMessages {

    private PluginMessages() {
    }

    /**
     * Records a channel the client registered. The set lives on the connection, so channels registered
     * during configuration carry over to the game phase. The event fires only when the set changed.
     */
    public static void registerChannel(final Connection connection, final @Nullable CraftPlayer player, final String channel) {
        final Set<String> channels = connection.paper$pluginMessagerChannels();
        Preconditions.checkState(PluginMessageBridgeImpl.DISABLE_CHANNEL_LIMIT || channels.size() < PluginMessageBridgeImpl.CHANNEL_LIMIT, "Cannot register channel. Too many channels registered!");
        final String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        if (channels.add(corrected) && player != null) {
            Bukkit.getPluginManager().callEvent(new PlayerRegisterChannelEvent(player, corrected));
        }
    }

    public static void unregisterChannel(final Connection connection, final @Nullable CraftPlayer player, final String channel) {
        final String corrected = StandardMessenger.validateAndCorrectChannel(channel);
        if (connection.paper$pluginMessagerChannels().remove(corrected) && player != null) {
            Bukkit.getPluginManager().callEvent(new PlayerUnregisterChannelEvent(player, corrected));
        }
    }
}