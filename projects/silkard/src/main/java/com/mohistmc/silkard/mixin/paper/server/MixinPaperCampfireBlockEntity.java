package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.injected.paper.server.PaperCampfireBlockEntity;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class MixinPaperCampfireBlockEntity implements PaperCampfireBlockEntity {

    @Unique private static final String SILKARD$STOP_COOKING_KEY = "Paper.StopCooking";

    @Unique private final boolean[] silkard$stopCooking = new boolean[4];

    @Override
    public boolean[] silkard$stopCooking() {
        return this.silkard$stopCooking;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void silkard$loadStopCooking(ValueInput input, CallbackInfo ci) {
        input.read(SILKARD$STOP_COOKING_KEY, Codec.BYTE_BUFFER).ifPresent(bytes -> {
            final boolean[] cookingState = new boolean[4];
            for (int index = 0; bytes.hasRemaining() && index < cookingState.length; index++) {
                cookingState[index] = bytes.get() == 1;
            }
            System.arraycopy(cookingState, 0, this.silkard$stopCooking, 0, Math.min(this.silkard$stopCooking.length, bytes.capacity()));
        });
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void silkard$saveStopCooking(ValueOutput output, CallbackInfo ci) {
        byte[] cookingState = new byte[4];
        for (int index = 0; index < cookingState.length; index++) {
            cookingState[index] = (byte) (this.silkard$stopCooking[index] ? 1 : 0);
        }
        output.store(SILKARD$STOP_COOKING_KEY, Codec.BYTE_BUFFER, ByteBuffer.wrap(cookingState));
    }
}
