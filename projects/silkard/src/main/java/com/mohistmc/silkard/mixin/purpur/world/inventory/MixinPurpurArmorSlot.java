package com.mohistmc.silkard.mixin.purpur.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Config to remove curse of binding with weakness: weakened players can take bound armor off. */
@Mixin(ArmorSlot.class)
public abstract class MixinPurpurArmorSlot {

    @ModifyExpressionValue(method = "mayPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;has(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Z"), require = 1)
    private boolean purpur$bindingWithWeakness(boolean bound, Player player) {
        return bound && !(player.level().purpurConfig().playerRemoveBindingWithWeakness && player.hasEffect(MobEffects.WEAKNESS));
    }
}
