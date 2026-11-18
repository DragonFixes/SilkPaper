package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper's bee rolling override: a set value replaces the vanilla rolling flag. */
@Mixin(Bee.class)
public abstract class MixinPaperBee implements PaperMobExtras.Bee {

    @Unique private TriState paper$rollingOverride = TriState.NOT_SET;

    @Inject(method = "isRolling", at = @At("HEAD"), cancellable = true)
    private void paper$rollingOverride(CallbackInfoReturnable<Boolean> cir) {
        if (this.paper$rollingOverride != TriState.NOT_SET) {
            cir.setReturnValue(this.paper$rollingOverride == TriState.TRUE);
        }
    }

    @Override
    public TriState paper$rollingOverride() {
        return this.paper$rollingOverride;
    }

    @Override
    public void paper$setRollingOverride(TriState rolling) {
        this.paper$rollingOverride = rolling;
    }
}
