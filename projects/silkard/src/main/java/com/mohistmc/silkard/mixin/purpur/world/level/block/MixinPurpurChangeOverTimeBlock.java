package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Purpur - option to disable the copper oxidation proximity penalty. The proximity chance becomes
 * 1, so the result is the plain chance modifier, as if no neighbour slowed the ageing down.
 */
@Mixin(ChangeOverTimeBlock.class)
public interface MixinPurpurChangeOverTimeBlock {

    @ModifyVariable(method = "getNextState", at = @At("STORE"), ordinal = 0)
    private float purpur$disableProximityPenalty(float chance, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        return level.purpurConfig().disableOxidationProximityPenalty ? 1.0F : chance;
    }
}
