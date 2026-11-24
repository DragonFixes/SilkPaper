package com.mohistmc.silkard.mixin.purpur.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Set name visible when using a Name Tag on an Armor Stand. */
@Mixin(NameTagItem.class)
public abstract class MixinPurpurNameTagItem {

    @Inject(method = "interactLivingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setCustomName(Lnet/minecraft/network/chat/Component;)V", shift = At.Shift.AFTER))
    private void purpur$armorStandNameVisible(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().purpurConfig().armorstandFixNametags && target instanceof ArmorStand) {
            target.setCustomNameVisible(true);
        }
    }
}
