package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.block.KelpBlock;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - configurable max growth age, see {@link MixinPurpurGrowingPlantHeadBlock}. */
@Mixin(KelpBlock.class)
public abstract class MixinPurpurKelpBlock extends MixinPurpurGrowingPlantHeadBlock {

    @Override
    public int getMaxGrowthAge() {
        return org.purpurmc.purpur.PurpurConfig.kelpMaxGrowthAge;
    }
}
