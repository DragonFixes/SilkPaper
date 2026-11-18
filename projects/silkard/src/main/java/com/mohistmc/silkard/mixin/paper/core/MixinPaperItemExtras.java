package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import java.util.function.Predicate;
import net.kyori.adventure.pointer.Pointers;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper state on vanilla item, menu and damage classes; see {@link PaperItemExtras}. */
public final class MixinPaperItemExtras {

    private MixinPaperItemExtras() {
    }

    /** A predicate ingredient matches exactly the stacks its predicate accepts. */
    @Mixin(value = net.minecraft.world.item.crafting.Ingredient.class, priority = 900)
    public abstract static class Ingredient implements PaperItemExtras.Ingredient {
        @Unique private @Nullable Predicate<ItemStack> paper$stackPredicate;
        @Unique private @Nullable ItemStack paper$predicateExample;

        @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
        private void paper$testPredicate(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
            if (this.paper$stackPredicate != null) {
                cir.setReturnValue(this.paper$stackPredicate.test(input));
            }
        }

        @Override
        public @Nullable Predicate<ItemStack> paper$stackPredicate() {
            return this.paper$stackPredicate;
        }

        @Override
        public @Nullable ItemStack paper$predicateExample() {
            return this.paper$predicateExample;
        }

        @Override
        public void paper$setStackPredicate(@Nullable Predicate<ItemStack> predicate, @Nullable ItemStack example) {
            this.paper$stackPredicate = predicate;
            this.paper$predicateExample = example;
        }
    }

    /** An offer that ignores discounts never gains a special price from reputation or effects. */
    @Mixin(net.minecraft.world.item.trading.MerchantOffer.class)
    public abstract static class MerchantOffer implements PaperItemExtras.MerchantOffer {
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

    /**
     * Paper's anvil overrides.
     * TODO Silkard - the anvil result calculation does not consult these overrides yet; they are stored only.
     */
    @Mixin(net.minecraft.world.inventory.AnvilMenu.class)
    public abstract static class AnvilMenu implements PaperItemExtras.AnvilMenu {
        @Unique private boolean paper$bypassEnchantmentLevelRestriction;
        @Unique private boolean paper$bypassCost;
        @Unique private boolean paper$canDoUnsafeEnchants;

        @Override
        public boolean paper$bypassEnchantmentLevelRestriction() {
            return this.paper$bypassEnchantmentLevelRestriction;
        }

        @Override
        public void paper$setBypassEnchantmentLevelRestriction(boolean bypass) {
            this.paper$bypassEnchantmentLevelRestriction = bypass;
        }

        @Override
        public boolean paper$bypassCost() {
            return this.paper$bypassCost;
        }

        @Override
        public void paper$setBypassCost(boolean bypass) {
            this.paper$bypassCost = bypass;
        }

        @Override
        public boolean paper$canDoUnsafeEnchants() {
            return this.paper$canDoUnsafeEnchants;
        }

        @Override
        public void paper$setCanDoUnsafeEnchants(boolean unsafe) {
            this.paper$canDoUnsafeEnchants = unsafe;
        }
    }

    /** The Adventure damage context a damage source was built with through the API. */
    @Mixin(net.minecraft.world.damagesource.DamageSource.class)
    public abstract static class DamageSource implements PaperItemExtras.DamageSource {
        @Unique private Pointers paper$damageContext = Pointers.empty();

        @Override
        public Pointers paper$damageContext() {
            return this.paper$damageContext;
        }

        @Override
        public void paper$setDamageContext(Pointers context) {
            this.paper$damageContext = context;
        }
    }
}
