package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface PurpurCropBlockAccessor {

    @Invoker("getBaseSeedId")
    ItemLike purpur$getBaseSeedId();
}
