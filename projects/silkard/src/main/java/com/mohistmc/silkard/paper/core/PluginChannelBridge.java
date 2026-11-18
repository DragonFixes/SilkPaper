package com.mohistmc.silkard.paper.core;

import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ClientboundConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feeds the client's {@code minecraft:register} and {@code minecraft:unregister} channel lists into
 * the Bukkit plugin channel set.
 *
 * <p>Fabric API decodes those payloads as its own registration payload type, so they never reach
 * Bukkit's custom payload handling. Its channel events report the same lists for both the
 * configuration and game phase; the channels are stored on the connection, which both phases share,
 * and in the game phase they go through {@link CraftPlayer#addChannel} so plugins see
 * {@code PlayerRegisterChannelEvent}.
 */
public final class PluginChannelBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("Silkard");

    private PluginChannelBridge() {
    }

    /** Registers the Fabric event listeners; call once during mod initialization. */
    public static void register() {
        ClientboundConfigurationChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            for (Identifier channel : channels) {
                handler.connection.paper$pluginMessagerChannels().add(channel.toString());
            }
        });
        ClientboundConfigurationChannelEvents.UNREGISTER.register((handler, sender, server, channels) -> {
            for (Identifier channel : channels) {
                handler.connection.paper$pluginMessagerChannels().remove(channel.toString());
            }
        });
        ClientboundPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> update(handler, channels, true));
        ClientboundPlayChannelEvents.UNREGISTER.register((handler, sender, server, channels) -> update(handler, channels, false));
    }

    private static void update(ServerGamePacketListenerImpl handler, List<Identifier> channels, boolean add) {
        CraftPlayer player = handler.getCraftPlayer();
        for (Identifier channel : channels) {
            String name = channel.toString();
            if (player == null) {
                if (add) {
                    handler.connection.paper$pluginMessagerChannels().add(name);
                } else {
                    handler.connection.paper$pluginMessagerChannels().remove(name);
                }
                continue;
            }
            try {
                if (add) {
                    player.addChannel(name);
                } else {
                    player.removeChannel(name);
                }
            } catch (IllegalStateException | IllegalArgumentException ex) {
                // Channel limit reached or a name Bukkit rejects; the client keeps the channel for Fabric.
                LOGGER.debug("Ignoring plugin channel {} for {}: {}", name, player.getName(), ex.getMessage());
            }
        }
    }
}
