package com.mohistmc.silkard.mixin.purpur.world.level.material;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Make lava flow speed configurable, for fast-lava dimensions and the rest. */
@Mixin(LavaFluid.class)
public abstract class MixinPurpurLavaFluid {

    @ModifyConstant(method = "getTickDelay", constant = @Constant(intValue = 10))
    private int purpur$lavaSpeedNether(int original, LevelReader level) {
        Level world = level.getWorldBorder().silkard$world();
        return world != null ? world.purpurConfig().lavaSpeedNether : original;
    }

    @ModifyConstant(method = "getTickDelay", constant = @Constant(intValue = 30))
    private int purpur$lavaSpeedNotNether(int original, LevelReader level) {
        Level world = level.getWorldBorder().silkard$world();
        return world != null ? world.purpurConfig().lavaSpeedNotNether : original;
    }
}
