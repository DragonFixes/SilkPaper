package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Cauldron fill chances for water and lava dripping from pointed dripstone. */
@Mixin(PointedDripstoneBlock.class)
public abstract class MixinPurpurPointedDripstoneBlock {

    @ModifyConstant(method = "maybeTransferFluid", constant = @Constant(floatValue = 0.17578125F), require = 2)
    private static float purpur$waterFillChance(float original, BlockState state, ServerLevel level, BlockPos pos, float randomValue) {
        return level.purpurConfig().cauldronDripstoneWaterFillChance;
    }

    @ModifyConstant(method = "maybeTransferFluid", constant = @Constant(floatValue = 0.05859375F), require = 2)
    private static float purpur$lavaFillChance(float original, BlockState state, ServerLevel level, BlockPos pos, float randomValue) {
        return level.purpurConfig().cauldronDripstoneLavaFillChance;
    }
}
