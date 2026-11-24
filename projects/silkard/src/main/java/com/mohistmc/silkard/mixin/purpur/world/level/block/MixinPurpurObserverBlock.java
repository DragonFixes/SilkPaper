package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Add Option for disable observer clocks: two observers facing each other stop pulsing. */
@Mixin(ObserverBlock.class)
public abstract class MixinPurpurObserverBlock {

    @WrapWithCondition(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ObserverBlock;startSignal(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/ScheduledTickAccess;Lnet/minecraft/core/BlockPos;)V"))
    private boolean purpur$disableObserverClocks(ObserverBlock self, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                                  BlockState state, LevelReader level2, ScheduledTickAccess ticks2, BlockPos pos2,
                                                  Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        Level world = level.getWorldBorder().silkard$world();
        return !(world != null && world.purpurConfig().disableObserverClocks)
            || !(neighbourState.getBlock() instanceof ObserverBlock)
            || neighbourState.getValue(ObserverBlock.FACING).getOpposite() != directionToNeighbour;
    }
}
