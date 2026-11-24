package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - place end crystal on any block, not only obsidian and bedrock. */
@Mixin(EndCrystalItem.class)
public abstract class MixinPurpurEndCrystalItem {

    @ModifyExpressionValue(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean purpur$placeAnywhere(boolean isObsidian, UseOnContext context) {
        return isObsidian || context.getLevel().purpurConfig().endCrystalPlaceAnywhere;
    }
}
