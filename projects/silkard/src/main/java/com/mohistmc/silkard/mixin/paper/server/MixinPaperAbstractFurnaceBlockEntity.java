package com.mohistmc.silkard.mixin.paper.server;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.silkard.injected.paper.server.PaperAbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinPaperAbstractFurnaceBlockEntity implements PaperAbstractFurnaceBlockEntity {

    @Unique private static final String SILKARD$COOK_SPEED_KEY = "Paper.CookSpeedMultiplier";

    @Unique private double silkard$cookSpeedMultiplier = 1.0;

    @Override
    public double silkard$cookSpeedMultiplier() {
        return this.silkard$cookSpeedMultiplier;
    }

    @Override
    public void silkard$setCookSpeedMultiplier(double multiplier) {
        this.silkard$cookSpeedMultiplier = multiplier;
    }

    @ModifyReturnValue(method = "getTotalCookTime(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I", at = @At("RETURN"))
    private static int silkard$scaleCookTime(int original, @Local(argsOnly = true) AbstractFurnaceBlockEntity entity) {
        double multiplier = ((PaperAbstractFurnaceBlockEntity) entity).silkard$cookSpeedMultiplier();
        return multiplier != 1.0D ? (int) Math.ceil(original / multiplier) : original;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void silkard$loadCookSpeed(ValueInput input, CallbackInfo ci) {
        this.silkard$cookSpeedMultiplier = input.getDoubleOr(SILKARD$COOK_SPEED_KEY, 1.0);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void silkard$saveCookSpeed(ValueOutput output, CallbackInfo ci) {
        output.putDouble(SILKARD$COOK_SPEED_KEY, this.silkard$cookSpeedMultiplier);
    }
}
