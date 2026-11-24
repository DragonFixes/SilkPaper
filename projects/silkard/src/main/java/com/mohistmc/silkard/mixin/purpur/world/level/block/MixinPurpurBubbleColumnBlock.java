package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.level.block.BubbleColumnBlock;
import org.purpurmc.purpur.PurpurConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Purpur - Config to reverse bubble column flow over soul sand and magma blocks. */
@Mixin(BubbleColumnBlock.class)
public abstract class MixinPurpurBubbleColumnBlock {

    private static final String SET_VALUE = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;";

    @ModifyArg(method = "getColumnState", at = @At(value = "INVOKE", target = SET_VALUE, ordinal = 0), index = 1)
    private static Comparable<?> purpur$soulSandFlow(Comparable<?> dragDown) {
        return PurpurConfig.soulSandBlockReverseBubbleColumnFlow;
    }

    @ModifyArg(method = "getColumnState", at = @At(value = "INVOKE", target = SET_VALUE, ordinal = 1), index = 1)
    private static Comparable<?> purpur$magmaFlow(Comparable<?> dragDown) {
        return !PurpurConfig.magmaBlockReverseBubbleColumnFlow;
    }
}
