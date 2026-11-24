package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Implement bed explosion options for beds used where they cannot set a spawn. */
@Mixin(BedBlock.class)
public abstract class MixinPurpurBedBlock {

    @WrapOperation(method = "destroyOnUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
    private void purpur$explosionOptions(Level level, Entity source, DamageSource damageSource, ExplosionDamageCalculator calculator, Vec3 pos,
                                         float power, boolean fire, Level.ExplosionInteraction interaction, Operation<Void> original) {
        if (!level.purpurConfig().bedExplode) return;
        original.call(level, source, damageSource, calculator, pos, (float) level.purpurConfig().bedExplosionPower,
            level.purpurConfig().bedExplosionFire, level.purpurConfig().bedExplosionEffect);
    }
}
