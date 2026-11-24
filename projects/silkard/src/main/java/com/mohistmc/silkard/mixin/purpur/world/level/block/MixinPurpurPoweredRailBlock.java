package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Config for powered rail activation distance. */
@Mixin(PoweredRailBlock.class)
public abstract class MixinPurpurPoweredRailBlock {

    @ModifyConstant(method = "findPoweredRailSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZI)Z", constant = @Constant(intValue = 8))
    private int purpur$railActivationRange(int original, Level level, BlockPos pos, BlockState state, boolean forward, int searchDepth) {
        return level.purpurConfig().railActivationRange;
    }
}
