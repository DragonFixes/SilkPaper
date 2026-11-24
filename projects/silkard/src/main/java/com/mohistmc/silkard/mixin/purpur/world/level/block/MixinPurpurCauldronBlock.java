package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Cauldron fill chances. */
@Mixin(CauldronBlock.class)
public abstract class MixinPurpurCauldronBlock {

    @ModifyConstant(method = "shouldHandlePrecipitation", constant = @Constant(floatValue = 0.05F))
    private static float purpur$rainChance(float original, Level level, Biome.Precipitation precipitation) {
        return level.purpurConfig().cauldronRainChance;
    }

    @ModifyConstant(method = "shouldHandlePrecipitation", constant = @Constant(floatValue = 0.1F))
    private static float purpur$powderSnowChance(float original, Level level, Biome.Precipitation precipitation) {
        return level.purpurConfig().cauldronPowderSnowChance;
    }
}
