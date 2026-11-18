package com.mohistmc.silkard.mixin.paper.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.injected.paper.core.PaperLivingEntity;
import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.kyori.adventure.util.TriState;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Purpur daylight burning API (a per-mob override of the {@code burn_in_daylight} entity tag) and
 * Paper's per-mob override of despawning in peaceful difficulty.
 */
@Mixin(Mob.class)
public abstract class MixinPaperMob implements PaperMobExtras.Mob {

    @Unique private TriState paper$despawnInPeacefulOverride = TriState.NOT_SET;

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean paper$shouldBurnInDay(Mob self, TagKey<EntityType<?>> tag, Operation<Boolean> original) {
        Boolean override = ((PaperLivingEntity) self).paper$shouldBurnInDayOverride();
        return override != null ? override : original.call(self, tag);
    }

    // TRUE forces despawning in peaceful, FALSE keeps the mob; the type decides otherwise.
    @WrapOperation(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;isAllowedInPeaceful()Z"))
    private boolean paper$despawnInPeaceful(EntityType<?> type, Operation<Boolean> original) {
        if (this.paper$despawnInPeacefulOverride == TriState.NOT_SET) {
            return original.call(type);
        }
        return this.paper$despawnInPeacefulOverride == TriState.FALSE;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveDespawnOverride(ValueOutput output, CallbackInfo ci) {
        if (this.paper$despawnInPeacefulOverride != TriState.NOT_SET) {
            output.putString("Paper.DespawnInPeacefulOverride", this.paper$despawnInPeacefulOverride.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$loadDespawnOverride(ValueInput input, CallbackInfo ci) {
        input.getString("Paper.DespawnInPeacefulOverride").ifPresent(name -> {
            try {
                this.paper$despawnInPeacefulOverride = TriState.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                this.paper$despawnInPeacefulOverride = TriState.NOT_SET;
            }
        });
    }

    @Override
    public TriState paper$despawnInPeacefulOverride() {
        return this.paper$despawnInPeacefulOverride;
    }

    @Override
    public void paper$setDespawnInPeacefulOverride(TriState state) {
        this.paper$despawnInPeacefulOverride = state;
    }
}
