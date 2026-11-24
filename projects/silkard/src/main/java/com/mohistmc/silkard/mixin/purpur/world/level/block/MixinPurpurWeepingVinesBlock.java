package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.block.WeepingVinesBlock;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - configurable max growth age, see {@link MixinPurpurGrowingPlantHeadBlock}. */
@Mixin(WeepingVinesBlock.class)
public abstract class MixinPurpurWeepingVinesBlock extends MixinPurpurGrowingPlantHeadBlock {

    @Override
    public int getMaxGrowthAge() {
        return org.purpurmc.purpur.PurpurConfig.weepingVinesMaxGrowthAge;
    }
}
