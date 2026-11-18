package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** An offer that ignores discounts never gains a special price from reputation or effects. */
@Mixin(net.minecraft.world.item.trading.MerchantOffer.class)
public abstract class MixinPaperMerchantOffer implements PaperItemExtras.MerchantOffer {
    @Unique private boolean paper$ignoreDiscounts;

    @Inject(method = "addToSpecialPriceDiff", at = @At("HEAD"), cancellable = true)
    private void paper$ignoreDiscountAdd(int add, CallbackInfo ci) {
        if (this.paper$ignoreDiscounts) {
            ci.cancel();
        }
    }

    @Inject(method = "setSpecialPriceDiff", at = @At("HEAD"), cancellable = true)
    private void paper$ignoreDiscountSet(int value, CallbackInfo ci) {
        if (this.paper$ignoreDiscounts) {
            ci.cancel();
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void paper$copyIgnoreDiscounts(CallbackInfoReturnable<net.minecraft.world.item.trading.MerchantOffer> cir) {
        cir.getReturnValue().paper$setIgnoreDiscounts(this.paper$ignoreDiscounts);
    }

    @Override
    public boolean paper$ignoreDiscounts() {
        return this.paper$ignoreDiscounts;
    }

    @Override
    public void paper$setIgnoreDiscounts(boolean ignoreDiscounts) {
        this.paper$ignoreDiscounts = ignoreDiscounts;
    }
}
