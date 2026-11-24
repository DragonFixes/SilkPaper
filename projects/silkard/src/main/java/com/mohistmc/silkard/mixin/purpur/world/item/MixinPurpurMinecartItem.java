package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Minecart settings: minecarts can be placed off rails. Clicking a solid block places
 * the minecart against the clicked face instead of inside the block.
 */
@Mixin(MinecartItem.class)
public abstract class MixinPurpurMinecartItem {

    @ModifyExpressionValue(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean purpur$placeAnywhere(boolean isRail, UseOnContext context, @Local BlockState blockState, @Local LocalRef<BlockPos> pos) {
        if (isRail || !context.getLevel().purpurConfig().minecartPlaceAnywhere) return isRail;
        if (blockState.isSolid()) pos.set(pos.get().relative(context.getClickedFace()));
        return true;
    }
}
