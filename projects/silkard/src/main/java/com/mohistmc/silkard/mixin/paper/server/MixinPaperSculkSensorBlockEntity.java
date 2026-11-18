package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.injected.paper.server.PaperSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlockEntity.class)
public abstract class MixinPaperSculkSensorBlockEntity implements PaperSculkSensorBlockEntity {

    @Unique private static final String SILKARD$LISTENER_RANGE_KEY = "Paper.ListenerRange";

    @Unique private @Nullable Integer silkard$rangeOverride;

    @Override
    public @Nullable Integer silkard$rangeOverride() {
        return this.silkard$rangeOverride;
    }

    @Override
    public void silkard$setRangeOverride(@Nullable Integer range) {
        this.silkard$rangeOverride = range;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void silkard$loadRange(ValueInput input, CallbackInfo ci) {
        this.silkard$rangeOverride = input.getInt(SILKARD$LISTENER_RANGE_KEY).orElse(null);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void silkard$saveRange(ValueOutput output, CallbackInfo ci) {
        // Only an override that differs from the vanilla range is worth storing.
        if (this.silkard$rangeOverride != null && this.silkard$rangeOverride != SculkSensorBlockEntity.VibrationUser.LISTENER_RANGE) {
            output.putInt(SILKARD$LISTENER_RANGE_KEY, this.silkard$rangeOverride);
        }
    }
}
