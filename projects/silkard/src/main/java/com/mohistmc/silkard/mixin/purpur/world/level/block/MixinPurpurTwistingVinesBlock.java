package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.block.TwistingVinesBlock;
import org.spongepowered.asm.mixin.Mixin;

/** Purpur - configurable max growth age, see {@link MixinPurpurGrowingPlantHeadBlock}. */
@Mixin(TwistingVinesBlock.class)
public abstract class MixinPurpurTwistingVinesBlock extends MixinPurpurGrowingPlantHeadBlock {

    @Override
    public int getMaxGrowthAge() {
        return org.purpurmc.purpur.PurpurConfig.twistingVinesMaxGrowthAge;
    }
}
