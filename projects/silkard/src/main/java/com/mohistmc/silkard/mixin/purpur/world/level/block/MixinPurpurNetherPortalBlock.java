package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Piglin portal spawn modifier. */
@Mixin(NetherPortalBlock.class)
public abstract class MixinPurpurNetherPortalBlock {

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 2000))
    private int purpur$piglinPortalSpawnModifier(int original, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        return level.purpurConfig().piglinPortalSpawnModifier;
    }
}
