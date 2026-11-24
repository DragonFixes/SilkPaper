package com.mohistmc.silkard.mixin.purpur.world.food;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.silkard.purpur.PurpurBurpHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Purpur - Burp delay. With a delay set, eating schedules the burp on the player instead of
 * playing it at once; the burp volume is configurable either way.
 */
@Mixin(FoodProperties.class)
public abstract class MixinPurpurFoodProperties {

    @WrapWithCondition(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 1), require = 1)
    private boolean purpur$delayBurp(Level level, Entity except, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch,
                                     @Local(argsOnly = true) LivingEntity user) {
        if (sound == SoundEvents.PLAYER_BURP && user instanceof Player player && level.purpurConfig().playerBurpDelay > 0) {
            ((PurpurBurpHolder) player).purpur$setBurpDelay(level.purpurConfig().playerBurpDelay);
            return false;
        }
        return true;
    }

    @ModifyConstant(method = "onConsume", constant = @Constant(floatValue = 0.5F), require = 1)
    private float purpur$burpVolume(float original, Level level) {
        return level.purpurConfig().playerBurpVolume;
    }
}
