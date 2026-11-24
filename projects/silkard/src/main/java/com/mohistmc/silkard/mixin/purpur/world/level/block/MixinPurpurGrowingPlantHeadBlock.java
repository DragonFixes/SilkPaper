package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Purpur - kelp, cave, weeping, and twisting configurable max growth age. Every vanilla use of the
 * maximum age 25 reads {@link #getMaxGrowthAge()} instead, which each plant overrides.
 */
@Mixin(GrowingPlantHeadBlock.class)
public abstract class MixinPurpurGrowingPlantHeadBlock {

    public int getMaxGrowthAge() {
        return GrowingPlantHeadBlock.MAX_AGE;
    }

    @Inject(method = "getStateForPlacement(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), cancellable = true)
    private void purpur$getStateForPlacement(RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        int max = this.getMaxGrowthAge();
        cir.setReturnValue(((GrowingPlantHeadBlock) (Object) this).defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, max == 0 ? 0 : random.nextInt(max)));
    }

    @ModifyConstant(method = {"isRandomlyTicking", "randomTick", "getMaxAgeState", "performBonemeal"}, constant = @Constant(intValue = 25))
    private int purpur$maxGrowthAge(int original) {
        return this.getMaxGrowthAge();
    }

    // Ages above a lowered maximum count as fully grown.
    @Inject(method = "isMaxAge", at = @At("HEAD"), cancellable = true)
    private void purpur$isMaxAge(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(state.getValue(GrowingPlantHeadBlock.AGE) >= this.getMaxGrowthAge());
    }
}
