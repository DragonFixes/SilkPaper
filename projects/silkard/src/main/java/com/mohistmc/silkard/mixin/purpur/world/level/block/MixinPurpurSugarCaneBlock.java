package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - bonemealable sugarcane: bone meal grows the cane to Paper's max reed height. */
@Mixin(SugarCaneBlock.class)
public abstract class MixinPurpurSugarCaneBlock implements BonemealableBlock {

    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state, final BonemealSource source) {
        if (!(level instanceof Level world) || !world.purpurConfig().sugarCanAffectedByBonemeal || !level.isEmptyBlock(pos.above())) return false;

        int reedHeight = 0;
        while (level.getBlockState(pos.below(reedHeight)).is((SugarCaneBlock) (Object) this)) {
            reedHeight++;
        }

        return reedHeight < world.paperConfig().maxGrowthHeight.reeds;
    }

    @Override
    public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        int reedHeight = 0;
        while (level.getBlockState(pos.below(reedHeight)).is((SugarCaneBlock) (Object) this)) {
            reedHeight++;
        }
        for (int i = 0; i <= level.paperConfig().maxGrowthHeight.reeds - reedHeight; i++) {
            level.setBlockAndUpdate(pos.above(i), state.setValue(SugarCaneBlock.AGE, 0));
        }
    }
}
