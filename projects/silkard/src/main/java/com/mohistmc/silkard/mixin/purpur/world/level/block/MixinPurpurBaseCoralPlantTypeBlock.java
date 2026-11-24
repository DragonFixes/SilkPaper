package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Config to not let coral die, for coral plants and fans. */
@Mixin(BaseCoralPlantTypeBlock.class)
public abstract class MixinPurpurBaseCoralPlantTypeBlock {

    @Inject(method = "scanForWater", at = @At("HEAD"), cancellable = true)
    private static void purpur$coralDieOutsideWater(BlockState state, BlockGetter level, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof LevelAccessor accessor && !accessor.getMinecraftWorld().purpurConfig().coralDieOutsideWater) {
            cir.setReturnValue(true);
        }
    }
}
