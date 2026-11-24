package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Purpur - Projectile offset config for crossbows. */
@Mixin(CrossbowItem.class)
public abstract class MixinPurpurCrossbowItem {

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FFLnet/minecraft/world/entity/LivingEntity;)V"), index = 5)
    private float purpur$projectileOffset(float uncertainty, @Local(argsOnly = true) Level level) {
        return (float) level.purpurConfig().crossbowProjectileOffset;
    }
}
