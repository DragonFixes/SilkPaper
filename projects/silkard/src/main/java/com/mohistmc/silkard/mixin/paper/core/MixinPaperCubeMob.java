package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paper's slime and magma cube wander switch. */
@Mixin(net.minecraft.world.entity.monster.cubemob.AbstractCubeMob.class)
public abstract class MixinPaperCubeMob implements PaperMobExtras.CubeMob {
    // TODO Silkard - the cube mob movement goals do not consult this switch yet
    @Unique private boolean paper$canWander = true;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$save(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        output.putBoolean("Paper.canWander", this.paper$canWander);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$load(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        this.paper$canWander = input.getBooleanOr("Paper.canWander", true);
    }

    @Override
    public boolean paper$canWander() {
        return this.paper$canWander;
    }

    @Override
    public void paper$setCanWander(boolean canWander) {
        this.paper$canWander = canWander;
    }
}
