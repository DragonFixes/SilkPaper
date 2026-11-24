package com.mohistmc.silkard.mixin.purpur.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Implement infinite liquids: how many neighbouring sources make a new source. */
@Mixin(FlowingFluid.class)
public abstract class MixinPurpurFlowingFluid {

    @ModifyConstant(method = "getNewLiquid", constant = @Constant(intValue = 2))
    private int purpur$requiredSources(int original, ServerLevel level, BlockPos pos, BlockState state) {
        Object self = this;
        if (self instanceof WaterFluid) return level.purpurConfig().waterInfiniteRequiredSources;
        if (self instanceof LavaFluid) return level.purpurConfig().lavaInfiniteRequiredSources;
        return original;
    }
}
