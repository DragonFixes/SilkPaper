package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemEntity;
import net.kyori.adventure.util.TriState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper's mob pickup switch and Purpur's item damage immunities on {@link ItemEntity}. The
 * immunities cancel damage of the matching kind and are saved under Purpur's keys.
 */
@Mixin(ItemEntity.class)
public abstract class MixinPaperItemEntity implements PaperItemEntity {

    @Unique private boolean paper$canMobPickup = true;
    @Unique private TriState paper$frictionState = TriState.NOT_SET;
    @Unique private boolean paper$immuneToCactus;
    @Unique private boolean paper$immuneToExplosion;
    @Unique private boolean paper$immuneToFire;
    @Unique private boolean paper$immuneToLightning;

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void paper$immunities(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if ((this.paper$immuneToCactus && source.is(DamageTypes.CACTUS))
            || (this.paper$immuneToExplosion && source.is(DamageTypeTags.IS_EXPLOSION))
            || (this.paper$immuneToFire && source.is(DamageTypeTags.IS_FIRE))
            || (this.paper$immuneToLightning && source.is(DamageTypes.LIGHTNING_BOLT))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void paper$fireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (this.paper$immuneToFire) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$save(ValueOutput output, CallbackInfo ci) {
        if (!this.paper$canMobPickup) {
            output.putBoolean("Paper.CanMobPickup", false);
        }
        if (this.paper$frictionState != TriState.NOT_SET) {
            output.putString("Paper.FrictionState", this.paper$frictionState.name());
        }
        if (this.paper$immuneToCactus) {
            output.putBoolean("Purpur.ImmuneToCactus", true);
        }
        if (this.paper$immuneToExplosion) {
            output.putBoolean("Purpur.ImmuneToExplosion", true);
        }
        if (this.paper$immuneToFire) {
            output.putBoolean("Purpur.ImmuneToFire", true);
        }
        if (this.paper$immuneToLightning) {
            output.putBoolean("Purpur.ImmuneToLightning", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$load(ValueInput input, CallbackInfo ci) {
        this.paper$canMobPickup = input.getBooleanOr("Paper.CanMobPickup", true);
        input.getString("Paper.FrictionState").ifPresent(name -> {
            try {
                this.paper$frictionState = TriState.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                this.paper$frictionState = TriState.NOT_SET;
            }
        });
        this.paper$immuneToCactus = input.getBooleanOr("Purpur.ImmuneToCactus", false);
        this.paper$immuneToExplosion = input.getBooleanOr("Purpur.ImmuneToExplosion", false);
        this.paper$immuneToFire = input.getBooleanOr("Purpur.ImmuneToFire", false);
        this.paper$immuneToLightning = input.getBooleanOr("Purpur.ImmuneToLightning", false);
    }

    @Override
    public boolean paper$canMobPickup() {
        return this.paper$canMobPickup;
    }

    @Override
    public void paper$setCanMobPickup(boolean canMobPickup) {
        // TODO Silkard - mob item pickup does not consult this flag yet; it is stored and saved only
        this.paper$canMobPickup = canMobPickup;
    }

    @Override
    public TriState paper$frictionState() {
        return this.paper$frictionState;
    }

    @Override
    public void paper$setFrictionState(TriState state) {
        // TODO Silkard - item movement does not apply Paper's friction override yet; it is stored and saved only
        this.paper$frictionState = state;
    }

    @Override
    public boolean paper$immuneToCactus() {
        return this.paper$immuneToCactus;
    }

    @Override
    public void paper$setImmuneToCactus(boolean immune) {
        this.paper$immuneToCactus = immune;
    }

    @Override
    public boolean paper$immuneToExplosion() {
        return this.paper$immuneToExplosion;
    }

    @Override
    public void paper$setImmuneToExplosion(boolean immune) {
        this.paper$immuneToExplosion = immune;
    }

    @Override
    public boolean paper$immuneToFire() {
        return this.paper$immuneToFire;
    }

    @Override
    public void paper$setImmuneToFire(boolean immune) {
        this.paper$immuneToFire = immune;
    }

    @Override
    public boolean paper$immuneToLightning() {
        return this.paper$immuneToLightning;
    }

    @Override
    public void paper$setImmuneToLightning(boolean immune) {
        this.paper$immuneToLightning = immune;
    }
}
