package com.mohistmc.silkard.mixin.purpur.world.entity.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mohistmc.silkard.purpur.PurpurBurpHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import org.purpurmc.purpur.PurpurWorldConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Purpur player options: burp delay, full netherite armor fire resistance, experience pickup
 * delay, critical damage multiplier and the death experience equation.
 */
@Mixin(Player.class)
public abstract class MixinPurpurPlayer implements PurpurBurpHolder {

    @Shadow public int experienceLevel;
    @Shadow public int totalExperience;
    @Shadow public float experienceProgress;

    @Unique private int purpur$burpDelay;

    @Override
    public void purpur$setBurpDelay(int ticks) {
        this.purpur$burpDelay = ticks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void purpur$tick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;
        PurpurWorldConfig config = level.purpurConfig();
        // Purpur - Burp delay
        if (this.purpur$burpDelay > 0 && --this.purpur$burpDelay == 0) {
            level.playSound(null, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, config.playerBurpVolume, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        // Purpur - Full netherite armor grants fire resistance
        if (config.playerNetheriteFireResistanceDuration > 0 && level.getGameTime() % 20 == 0
            && self.getItemBySlot(EquipmentSlot.HEAD).is(Items.NETHERITE_HELMET)
            && self.getItemBySlot(EquipmentSlot.CHEST).is(Items.NETHERITE_CHESTPLATE)
            && self.getItemBySlot(EquipmentSlot.LEGS).is(Items.NETHERITE_LEGGINGS)
            && self.getItemBySlot(EquipmentSlot.FEET).is(Items.NETHERITE_BOOTS)) {
            self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, config.playerNetheriteFireResistanceDuration, config.playerNetheriteFireResistanceAmplifier,
                config.playerNetheriteFireResistanceAmbient, config.playerNetheriteFireResistanceShowParticles, config.playerNetheriteFireResistanceShowIcon),
                org.bukkit.event.entity.EntityPotionEffectEvent.Cause.NETHERITE_ARMOR);
        }
    }

    // Purpur - Configurable player pickup exp delay: a negative delay means orbs are never picked up
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;is(Ljava/lang/Object;)Z", ordinal = 0), require = 1)
    private boolean purpur$pickupOrbs(boolean isOrb) {
        return isOrb && ((Player) (Object) this).level().purpurConfig().playerExpPickupDelay >= 0;
    }

    // Purpur - Add config change multiplier critical damage value
    @ModifyConstant(method = "attack", constant = @Constant(floatValue = 1.5F), require = 1)
    private float purpur$criticalMultiplier(float original) {
        return (float) ((Player) (Object) this).level().purpurConfig().playerCriticalDamageMultiplier;
    }

    // Purpur - Add player death exp control options, with Leaf's skip when the equation is the default
    @Inject(method = "getBaseExperienceReward", at = @At("HEAD"), cancellable = true)
    private void purpur$deathExp(ServerLevel level, CallbackInfoReturnable<Integer> cir) {
        Player self = (Player) (Object) this;
        if (level.getGameRules().get(GameRules.KEEP_INVENTORY) || self.isSpectator()) {
            cir.setReturnValue(0);
            return;
        }
        PurpurWorldConfig config = level.purpurConfig();
        int toDrop = this.experienceLevel * 7;
        if (config.playerDeathExpDropEquationEnabled) {
            try {
                toDrop = Math.round(((Number) com.mohistmc.silkard.paper.paper.PurpurScripts.eval(
                    "let expLevel = " + this.experienceLevel + "; let expTotal = " + this.totalExperience + "; let exp = " + this.experienceProgress + "; "
                        + config.playerDeathExpDropEquation)).floatValue());
            } catch (javax.script.ScriptException e) {
                e.printStackTrace();
            }
        }
        cir.setReturnValue(Math.min(toDrop, config.playerDeathExpDropMax));
    }
}
