package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Add allow water in end world option: water buckets evaporate in the End unless allowed. */
@Mixin(BucketItem.class)
public abstract class MixinPurpurBucketItem {

    @ModifyExpressionValue(method = "emptyContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;"))
    private Object purpur$waterEvaporatesInEnd(Object evaporates, @Local(argsOnly = true) Level level) {
        if (!org.purpurmc.purpur.PurpurConfig.allowWaterPlacementInTheEnd && level.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) {
            return Boolean.TRUE;
        }
        return evaporates;
    }
}
