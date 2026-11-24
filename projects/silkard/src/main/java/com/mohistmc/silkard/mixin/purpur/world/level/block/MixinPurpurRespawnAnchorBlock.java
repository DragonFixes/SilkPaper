package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Implement respawn anchor explosion options. */
@Mixin(RespawnAnchorBlock.class)
public abstract class MixinPurpurRespawnAnchorBlock {

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
    private void purpur$explosionOptions(ServerLevel level, Entity source, DamageSource damageSource, ExplosionDamageCalculator calculator, Vec3 pos,
                                         float power, boolean fire, Level.ExplosionInteraction interaction, Operation<Void> original) {
        if (!level.purpurConfig().respawnAnchorExplode) return;
        original.call(level, source, damageSource, calculator, pos, (float) level.purpurConfig().respawnAnchorExplosionPower,
            level.purpurConfig().respawnAnchorExplosionFire, level.purpurConfig().respawnAnchorExplosionEffect);
    }
}
