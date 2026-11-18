package com.mohistmc.silkard.paper.paper;

import com.mohistmc.silkard.injected.network.ContextDiscardedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;

/**
 * Builds outgoing plugin message packets. Vanilla's {@link DiscardedPayload} has no data component;
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
}
