package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A predicate ingredient matches exactly the stacks its predicate accepts. */
@Mixin(value = net.minecraft.world.item.crafting.Ingredient.class, priority = 900)
public abstract class MixinPaperIngredient implements PaperItemExtras.Ingredient {
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
