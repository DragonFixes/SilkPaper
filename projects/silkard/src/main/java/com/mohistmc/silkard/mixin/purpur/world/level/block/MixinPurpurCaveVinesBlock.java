package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.block.CaveVinesBlock;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - configurable max growth age, see {@link MixinPurpurGrowingPlantHeadBlock}. */
@Mixin(CaveVinesBlock.class)
public abstract class MixinPurpurCaveVinesBlock extends MixinPurpurGrowingPlantHeadBlock {

    @Override
    public int getMaxGrowthAge() {
        return org.purpurmc.purpur.PurpurConfig.caveVinesMaxGrowthAge;
    }
}
