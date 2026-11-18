package com.mohistmc.silkard.paper.core;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * A raw Bukkit plugin message: a channel id and the bytes a plugin handed to
 * {@code sendPluginMessage}, written to the wire unchanged.
 *
 * <p>The custom-payload codec looks up a codec by channel id, and Fabric API registers its own
 * payload types under ids such as {@code minecraft:register}. A plugin message on such a channel
 * would otherwise be handed to Fabric's codec and fail to encode, so
 * {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperCustomPayloadCodec} writes this payload
 * before any lookup happens. Payloads of Fabric mods never take that path.
 *
 * @param id the plugin channel
 * @param data the message bytes, written without a length prefix
 */
public record PluginMessagePayload(Identifier id, byte[] data) implements CustomPacketPayload {

    public PluginMessagePayload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(data, "data");
    }

    @Override
    public Type<PluginMessagePayload> type() {
        return new Type<>(this.id);
    }

    /** Writes the channel id followed by the raw message bytes, as the client expects them. */
    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(this.id);
        buf.writeBytes(this.data);
    }

    /** A clientbound custom-payload packet carrying the raw message. */
    public static ClientboundCustomPayloadPacket packet(Identifier id, byte[] data) {
        return new ClientboundCustomPayloadPacket(new PluginMessagePayload(id, data));
    }
}
