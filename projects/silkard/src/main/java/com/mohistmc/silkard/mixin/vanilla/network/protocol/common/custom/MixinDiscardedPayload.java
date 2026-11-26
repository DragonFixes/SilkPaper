package com.mohistmc.silkard.mixin.vanilla.network.protocol.common.custom;

import com.mohistmc.silkard.injected.network.ContextDiscardedPayload;
import io.izzel.arclight.mixin.CreateConstructor;
import io.izzel.arclight.mixin.ShadowConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper turns {@code DiscardedPayload} into {@code record DiscardedPayload(Identifier id, byte[] data)} so
 * plugin message bytes survive decoding and are written back out. Plugins compiled against Paper (Axiom,
 * for one) look up the {@code (Identifier, byte[])} constructor by reflection and call {@code data()}, so
 * both exist here with Paper's signatures.
 *
 * @author Mgazul
 * @date 2026/10/30
 */
@Mixin(DiscardedPayload.class)
public class MixinDiscardedPayload implements ContextDiscardedPayload {

    @Unique
    private static final byte[] silkard$EMPTY = new byte[0];

    @Unique
    private byte[] silkard$data = silkard$EMPTY;

    @ShadowConstructor
    public void silkard$constructor(Identifier id) {
        throw new AbstractMethodError();
    }

    @CreateConstructor
    public void silkard$constructor(Identifier id, byte[] data) {
        this.silkard$constructor(id);
        this.silkard$data = data == null ? silkard$EMPTY : data;
    }

    @Override
    public byte[] data() {
        return this.silkard$data;
    }

    @Override
    public void silkard$data(byte[] data) {
        this.silkard$data = data == null ? silkard$EMPTY : data;
    }

    /**
     * @author Mgazul
     * @reason Paper - keep payload bytes and write them back out
     */
    @Overwrite
    public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(Identifier id, int maxPayloadSize) {
        return CustomPacketPayload.codec((payload, buf) -> {
            buf.writeBytes(((ContextDiscardedPayload) (Object) payload).data());
        }, buf -> {
            int length = buf.readableBytes();
            if (length >= 0 && length <= maxPayloadSize) {
                byte[] data = new byte[length];
                buf.readBytes(data);
                DiscardedPayload payload = new DiscardedPayload(id);
                ((ContextDiscardedPayload) (Object) payload).silkard$data(data);
                return payload;
            }
            throw new IllegalArgumentException("Payload may not be larger than " + maxPayloadSize + " bytes");
        });
    }
}
