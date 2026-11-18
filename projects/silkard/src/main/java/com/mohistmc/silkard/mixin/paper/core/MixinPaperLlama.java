package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur's caravan switch: a llama that may not join a caravan never becomes a caravan member. */
@Mixin(Llama.class)
public abstract class MixinPaperLlama implements PaperMobExtras.Llama {

    @Unique private boolean paper$shouldJoinCaravan = true;

    @Inject(method = "inCaravan", at = @At("HEAD"), cancellable = true)
    private void paper$caravanSwitch(CallbackInfoReturnable<Boolean> cir) {
        if (!this.paper$shouldJoinCaravan) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$save(ValueOutput output, CallbackInfo ci) {
        if (!this.paper$shouldJoinCaravan) {
            output.putBoolean("Purpur.ShouldJoinCaravan", false);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$load(ValueInput input, CallbackInfo ci) {
        this.paper$shouldJoinCaravan = input.getBooleanOr("Purpur.ShouldJoinCaravan", true);
    }

    @Override
    public boolean paper$shouldJoinCaravan() {
        return this.paper$shouldJoinCaravan;
    }

    @Override
    public void paper$setShouldJoinCaravan(boolean shouldJoinCaravan) {
        this.paper$shouldJoinCaravan = shouldJoinCaravan;
        if (!shouldJoinCaravan) {
            ((Llama) (Object) this).leaveCaravan();
        }
    }
}
