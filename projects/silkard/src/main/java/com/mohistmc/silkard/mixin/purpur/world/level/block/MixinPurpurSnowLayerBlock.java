package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Add config for snow on blue ice. */
@Mixin(SnowLayerBlock.class)
public abstract class MixinPurpurSnowLayerBlock {

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void purpur$snowOnBlueIce(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!level.getBlockState(pos.below()).is(Blocks.BLUE_ICE)) return;
        Level world = com.mohistmc.silkard.purpur.PurpurHooks.level(level);
        if (world != null && !world.purpurConfig().snowOnBlueIce) {
            cir.setReturnValue(false);
        }
    }
}
