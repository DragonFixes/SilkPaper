package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Purpur - Projectile offset config for wind charges and throwable potions: the spread of the
 * thrown projectile. Snowballs, eggs and ender pearls set it in their CraftBukkit spawn hooks.
 */
@Mixin({WindChargeItem.class, ThrowablePotionItem.class})
public abstract class MixinPurpurProjectileItems {

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"), index = 6)
    private float purpur$projectileOffset(float uncertainty, @Local(argsOnly = true) Level level) {
        return (float) ((Object) this instanceof WindChargeItem
            ? level.purpurConfig().windChargeProjectileOffset
            : level.purpurConfig().throwablePotionProjectileOffset);
    }
}
