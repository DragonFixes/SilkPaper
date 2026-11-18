package com.mohistmc.silkard.paper.paper;

import com.mohistmc.silkard.injected.network.ContextDiscardedPayload;
import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import io.papermc.paper.connection.PluginMessageBridgeImpl;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jspecify.annotations.Nullable;

/**
 * Builds outgoing plugin message packets and tracks the client's registered channels. Vanilla's {@link DiscardedPayload} has no data component;
 * Silkard keeps the bytes on the payload and its codec writes them back out.
 */
public final class PluginMessages {

    private PluginMessages() {
    }

    public static DiscardedPayload payload(final Identifier channel, final byte[] message) {
        final DiscardedPayload payload = new DiscardedPayload(channel);
        ((ContextDiscardedPayload) (Object) payload).silkard$data(Unpooled.wrappedBuffer(message));
        return payload;
    }

    public static ClientboundCustomPayloadPacket packet(final Identifier channel, final byte[] message) {
        return new ClientboundCustomPayloadPacket(payload(channel, message));
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