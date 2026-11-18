package com.mohistmc.silkard.mixin.paper.server;

import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkSensorBlockEntity.VibrationUser.class)
public abstract class MixinPaperSculkSensorVibrationUser {

    @Shadow @Final SculkSensorBlockEntity this$0;

    @Inject(method = "getListenerRadius", at = @At("HEAD"), cancellable = true)
    private void silkard$rangeOverride(CallbackInfoReturnable<Integer> cir) {
        Integer override = this.this$0.silkard$rangeOverride();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
