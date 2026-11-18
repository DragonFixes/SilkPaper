package com.mohistmc.silkard.mixin.paper.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.injected.paper.core.PaperEntity;
import com.mohistmc.silkard.injected.paper.core.PaperLivingEntity;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper freeze lock and friction API, and Purpur's daylight burning override, on living entities.
 */
@Mixin(LivingEntity.class)
public abstract class MixinPaperLivingEntity implements PaperLivingEntity {

    @Unique private TriState paper$frictionState = TriState.NOT_SET;
    @Unique private @Nullable Boolean paper$shouldBurnInDay;

    /** A freeze-locked entity does not thaw outside powder snow. */
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setTicksFrozen(I)V"))
    private void paper$freezeLock(LivingEntity self, int ticks, Operation<Void> original) {
        if (!((PaperEntity) self).paper$freezeLocked()) {
            original.call(self, ticks);
        }
    }

    @Inject(method = "shouldDiscardFriction", at = @At("HEAD"), cancellable = true)
    private void paper$frictionOverride(CallbackInfoReturnable<Boolean> cir) {
        if (this.paper$frictionState != TriState.NOT_SET) {
            cir.setReturnValue(!this.paper$frictionState.toBooleanOrElse(true));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveData(ValueOutput output, CallbackInfo ci) {
        if (this.paper$frictionState != TriState.NOT_SET) {
            output.putString("Paper.FrictionState", this.paper$frictionState.toString());
        }
        if (this.paper$shouldBurnInDay != null) {
            output.putBoolean("Purpur.ShouldBurnInDay", this.paper$shouldBurnInDay);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$loadData(ValueInput input, CallbackInfo ci) {
        input.getString("Paper.FrictionState").ifPresent(state -> {
            try {
                this.paper$frictionState = TriState.valueOf(state);
            } catch (IllegalArgumentException ignored) {
                this.paper$frictionState = TriState.NOT_SET;
            }
        });
        input.read("Purpur.ShouldBurnInDay", com.mojang.serialization.Codec.BOOL).ifPresent(burn -> this.paper$shouldBurnInDay = burn);
    }

    @Override
    public TriState paper$frictionState() {
        return this.paper$frictionState;
    }

    @Override
    public void paper$setFrictionState(TriState state) {
        this.paper$frictionState = state;
    }

    @Override
    public @Nullable Boolean paper$shouldBurnInDayOverride() {
        return this.paper$shouldBurnInDay;
    }

    @Override
    public void paper$setShouldBurnInDayOverride(@Nullable Boolean shouldBurnInDay) {
        this.paper$shouldBurnInDay = shouldBurnInDay;
    }
}
