package com.mohistmc.silkard.mixin.purpur.world.item;

import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Implement elytra settings: each firework boost damages the glider being used. */
@Mixin(FireworkRocketItem.class)
public abstract class MixinPurpurFireworkRocketItem {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/Projectile;", shift = At.Shift.AFTER))
    private void purpur$elytraDamagePerBoost(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        int damage = level.purpurConfig().elytraDamagePerFireworkBoost;
        if (damage <= 0 || !player.canGlide()) return;
        List<EquipmentSlot> slots = EquipmentSlot.VALUES.stream().filter(slot -> LivingEntity.canGlideUsing(player.getItemBySlot(slot), slot)).toList();
        if (slots.isEmpty()) return;
        EquipmentSlot slot = Util.getRandom(slots, player.getRandom());
        player.getItemBySlot(slot).hurtAndBreak(damage, player, slot);
    }
}
