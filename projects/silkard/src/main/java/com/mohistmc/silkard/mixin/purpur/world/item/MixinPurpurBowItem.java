package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Purpur - Infinity bow settings and projectile offset config. With infinity-works-without-arrows,
 * an Infinity bow draws and fires a plain arrow when the player carries none.
 */
@Mixin(BowItem.class)
public abstract class MixinPurpurBowItem {

    @ModifyExpressionValue(method = {"use", "releaseUsing"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack purpur$infinityWithoutArrows(ItemStack projectile, @Local(argsOnly = true) Level level, @Local(argsOnly = true) ItemStack bow) {
        if (projectile.isEmpty() && level.purpurConfig().infinityWorksWithoutArrows
            && EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY), bow) > 0) {
            return new ItemStack(Items.ARROW);
        }
        return projectile;
    }

    @ModifyArg(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V"), index = 6)
    private float purpur$projectileOffset(float uncertainty, @Local(argsOnly = true) Level level) {
        return (float) level.purpurConfig().bowProjectileOffset;
    }
}
