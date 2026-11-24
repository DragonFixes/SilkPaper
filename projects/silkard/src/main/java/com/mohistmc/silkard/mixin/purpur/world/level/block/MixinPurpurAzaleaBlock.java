package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Purpur - Chance for azalea blocks to grow into trees naturally. Azaleas tick randomly so they can
 * roll the configured growth chance; with both chances at 0 the tick does nothing.
 */
@Mixin(AzaleaBlock.class)
public abstract class MixinPurpurAzaleaBlock extends VegetationBlock {

    private MixinPurpurAzaleaBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double chance = state.getBlock() == Blocks.FLOWERING_AZALEA ? level.purpurConfig().floweringAzaleaGrowthChance : level.purpurConfig().azaleaGrowthChance;
        if (chance > 0.0D && level.getMaxLocalRawBrightness(pos.above()) > 9 && random.nextDouble() < chance) {
            TreeGrower.AZALEA.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
        }
    }
}
