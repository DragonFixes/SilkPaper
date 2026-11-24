package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Cactus breaks from solid neighbors config, and bonemealable cactus. */
@Mixin(CactusBlock.class)
public abstract class MixinPurpurCactusBlock implements BonemealableBlock {

    @ModifyExpressionValue(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolid()Z"))
    private boolean purpur$breaksFromSolidNeighbors(boolean solid, BlockState state, LevelReader level, BlockPos pos) {
        Level world = com.mohistmc.silkard.purpur.PurpurHooks.level(level);
        return solid && (world == null || world.purpurConfig().cactusBreaksFromSolidNeighbors);
    }

    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state, final BonemealSource source) {
        if (!(level instanceof Level world) || !world.purpurConfig().cactusAffectedByBonemeal || !level.isEmptyBlock(pos.above())) return false;
        int cactusHeight = 0;
        while (level.getBlockState(pos.below(cactusHeight)).is((CactusBlock) (Object) this)) {
            cactusHeight++;
        }
        return cactusHeight < world.paperConfig().maxGrowthHeight.cactus;
    }

    @Override
    public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        int cactusHeight = 0;
        while (level.getBlockState(pos.below(cactusHeight)).is((CactusBlock) (Object) this)) {
            cactusHeight++;
        }
        for (int i = 0; i <= level.paperConfig().maxGrowthHeight.cactus - cactusHeight; i++) {
            level.setBlockAndUpdate(pos.above(i), state.setValue(CactusBlock.AGE, 0));
        }
    }
}
