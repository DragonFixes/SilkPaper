package com.mohistmc.silkard.mixin.purpur.world.item.consume_effects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Option to toggle milk curing bad omen: milk clears everything else and keeps it. */
@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public abstract class MixinPurpurClearAllStatusEffectsConsumeEffect {

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void purpur$keepBadOmen(Level level, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {
        MobEffectInstance badOmen = user.getEffect(MobEffects.BAD_OMEN);
        if (badOmen != null && !level.purpurConfig().milkCuresBadOmen && stack.is(Items.MILK_BUCKET)) {
            org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause = org.bukkit.event.entity.EntityPotionEffectEvent.Cause.MILK;
            cir.setReturnValue(user.removeAllEffects(cause) && user.addEffect(badOmen));
        }
    }
}
