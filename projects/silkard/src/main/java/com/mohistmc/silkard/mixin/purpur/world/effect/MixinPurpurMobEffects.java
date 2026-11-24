package com.mohistmc.silkard.mixin.purpur.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Purpur - Config MobEffect by world. Poison's damage, regeneration's healing and saturation's
 * food are set in their CraftBukkit hooks; this covers the constants those hooks do not pass on.
 */
public final class MixinPurpurMobEffects {

    private MixinPurpurMobEffects() {
    }

    /** Poison never takes a mob below this much health. */
    @Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
    public abstract static class Poison {
        @ModifyConstant(method = "applyEffectTick", constant = @Constant(floatValue = 1.0F, ordinal = 0), require = 1)
        private float purpur$minimalHealth(float original, ServerLevel level, LivingEntity mob, int amplification) {
            return mob.level().purpurConfig().entityMinimalHealthPoison;
        }
    }

    @Mixin(targets = "net.minecraft.world.effect.WitherMobEffect")
    public abstract static class Wither {
        @ModifyConstant(method = "applyEffectTick", constant = @Constant(floatValue = 1.0F), require = 1, allow = 1)
        private float purpur$witherDamage(float original, ServerLevel level, LivingEntity mob, int amplification) {
            return mob.level().purpurConfig().entityWitherDegenerationAmount;
        }
    }

    /** Exhaustion per hunger level, multiplied by the amplifier plus one as in vanilla. */
    @Mixin(targets = "net.minecraft.world.effect.HungerMobEffect")
    public abstract static class Hunger {
        @ModifyConstant(method = "applyEffectTick", constant = @Constant(floatValue = 0.005F), require = 1)
        private float purpur$exhaustion(float original, ServerLevel level, LivingEntity mob, int amplification) {
            return mob.level().purpurConfig().humanHungerExhaustionAmount;
        }
    }
}
