package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Tick fluids config: with it off, placed or updated fluids do not start flowing. */
@Mixin(LiquidBlock.class)
public abstract class MixinPurpurLiquidBlock {

    @ModifyExpressionValue(method = {"onPlace", "neighborChanged"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean purpur$tickFluids(boolean shouldSpread, @Local(argsOnly = true) Level level) {
        return shouldSpread && level.purpurConfig().tickFluids;
    }

    @ModifyExpressionValue(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;isSource()Z", ordinal = 0))
    private boolean purpur$tickFluidsOnUpdate(boolean isSource, @Local(argsOnly = true) LevelReader level) {
        Level world = level.getWorldBorder().silkard$world();
        return isSource && (world == null || world.purpurConfig().tickFluids);
    }
}
