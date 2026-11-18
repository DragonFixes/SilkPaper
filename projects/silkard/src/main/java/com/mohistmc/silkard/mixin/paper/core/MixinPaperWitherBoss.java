package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper's wither portal switch: when set, the wither may travel through portals. */
@Mixin(net.minecraft.world.entity.boss.wither.WitherBoss.class)
public abstract class MixinPaperWitherBoss implements PaperMobExtras.WitherBoss {
    @Unique private boolean paper$canTravelThroughPortals;

    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void paper$portalSwitch(boolean ignorePassenger, CallbackInfoReturnable<Boolean> cir) {
        if (this.paper$canTravelThroughPortals) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void paper$setCanTravelThroughPortals(boolean canTravel) {
        this.paper$canTravelThroughPortals = canTravel;
    }
}
