package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - bonemealable netherwart. */
@Mixin(NetherWartBlock.class)
public abstract class MixinPurpurNetherWartBlock implements BonemealableBlock {

    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return level instanceof Level world && world.purpurConfig().netherWartAffectedByBonemeal && state.getValue(NetherWartBlock.AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state, final BonemealSource source) {
        int age = Math.min(3, state.getValue(NetherWartBlock.AGE) + 1);
        org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockGrowEvent(level, pos, state.setValue(NetherWartBlock.AGE, age), 2); // CraftBukkit
    }
}
