package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperEntity;
import java.util.UUID;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper/Purpur entity state: pose lock, fire immunity and visual fire overrides, freeze lock,
 * removal cause, total age, origin and spawn reason, persisted under Paper's NBT keys.
 */
@Mixin(Entity.class)
public abstract class MixinPaperEntity implements PaperEntity {

    @Shadow @Final protected static int FLAG_ONFIRE;
    @Shadow private boolean hasVisualFire;

    @Shadow protected abstract void setSharedFlag(int flag, boolean value);

    @Unique private boolean paper$fixedPose;
    @Unique private @Nullable Boolean paper$immuneToFire;
    @Unique private TriState paper$visualFire = TriState.NOT_SET;
    @Unique private boolean paper$freezeLocked;
    @Unique private EntityRemoveEvent.@Nullable Cause paper$removeEventCause;
    @Unique private int paper$totalEntityAge;
    @Unique private @Nullable Vec3 paper$origin;
    @Unique private @Nullable UUID paper$originWorld;
    @Unique private boolean paper$spawnedViaMobSpawner;
    @Unique private CreatureSpawnEvent.@Nullable SpawnReason paper$spawnReason;

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void paper$keepFixedPose(Pose pose, CallbackInfo ci) {
        if (this.paper$fixedPose) {
            ci.cancel();
        }
    }

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void paper$fireImmunityOverride(CallbackInfoReturnable<Boolean> cir) {
        if (this.paper$immuneToFire != null) {
            cir.setReturnValue(this.paper$immuneToFire);
        }
    }

    @Inject(method = "setSharedFlagOnFire", at = @At("HEAD"), cancellable = true)
    private void paper$visualFireOverride(boolean value, CallbackInfo ci) {
        if (this.paper$visualFire != TriState.NOT_SET) {
            this.setSharedFlag(FLAG_ONFIRE, this.paper$visualFire.toBooleanOrElse(value));
            ci.cancel();
        }
    }

    @Inject(method = "clearFreeze", at = @At("HEAD"), cancellable = true)
    private void paper$freezeLock(CallbackInfo ci) {
        if (this.paper$freezeLocked) {
            ci.cancel();
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void paper$countAge(CallbackInfo ci) {
        this.paper$totalEntityAge++;
    }

    @Inject(method = "unsetRemoved", at = @At("HEAD"))
    private void paper$clearRemoveCause(CallbackInfo ci) {
        this.paper$removeEventCause = null;
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void paper$saveData(ValueOutput output, CallbackInfo ci) {
        output.putInt("Spigot.ticksLived", this.paper$totalEntityAge);
        if (this.paper$visualFire != TriState.NOT_SET) {
            output.putString("Paper.FireOverride", this.paper$visualFire.name());
        }
        if (this.paper$origin != null) {
            if (this.paper$originWorld != null) {
                output.store("Paper.OriginWorld", UUIDUtil.CODEC, this.paper$originWorld);
            }
            output.store("Paper.Origin", Vec3.CODEC, this.paper$origin);
        }
        if (this.paper$spawnReason != null) {
            output.putString("Paper.SpawnReason", this.paper$spawnReason.name());
        }
        if (this.paper$spawnedViaMobSpawner) {
            output.putBoolean("Paper.FromMobSpawner", true);
        }
        if (this.paper$freezeLocked) {
            output.putBoolean("Paper.FreezeLock", true);
        }
        if (this.paper$immuneToFire != null) {
            output.putBoolean("Purpur.FireImmune", this.paper$immuneToFire);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void paper$loadData(ValueInput input, CallbackInfo ci) {
        this.paper$totalEntityAge = input.getIntOr("Spigot.ticksLived", this.paper$totalEntityAge);
        input.getString("Paper.FireOverride").ifPresent(override -> {
            try {
                this.paper$setVisualFire(TriState.valueOf(override));
            } catch (IllegalArgumentException ignored) {
                // An unknown override keeps the vanilla HasVisualFire behavior.
            }
        });
        input.read("Paper.Origin", Vec3.CODEC).ifPresent(origin -> {
            this.paper$origin = origin;
            this.paper$originWorld = input.read("Paper.OriginWorld", UUIDUtil.CODEC).orElse(null);
        });
        this.paper$spawnedViaMobSpawner = input.getBooleanOr("Paper.FromMobSpawner", false);
        input.getString("Paper.SpawnReason").ifPresent(name -> {
            try {
                this.paper$spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                this.paper$spawnReason = null;
            }
        });
        if (this.paper$spawnReason == null) {
            this.paper$spawnReason = this.paper$spawnedViaMobSpawner ? CreatureSpawnEvent.SpawnReason.SPAWNER : CreatureSpawnEvent.SpawnReason.DEFAULT;
        }
        this.paper$freezeLocked = input.getBooleanOr("Paper.FreezeLock", false);
        input.read("Purpur.FireImmune", com.mojang.serialization.Codec.BOOL).ifPresent(immune -> this.paper$immuneToFire = immune);
    }

    @Override
    public boolean paper$fixedPose() {
        return this.paper$fixedPose;
    }

    @Override
    public void paper$setFixedPose(boolean fixedPose) {
        this.paper$fixedPose = fixedPose;
    }

    @Override
    public @Nullable Boolean paper$immuneToFire() {
        return this.paper$immuneToFire;
    }

    @Override
    public void paper$setImmuneToFire(@Nullable Boolean immuneToFire) {
        this.paper$immuneToFire = immuneToFire;
    }

    @Override
    public TriState paper$visualFire() {
        return this.paper$visualFire;
    }

    @Override
    public void paper$setVisualFire(TriState visualFire) {
        this.paper$visualFire = visualFire;
        // Vanilla still saves HasVisualFire; keep it in step for worlds opened without Silkard.
        this.hasVisualFire = visualFire == TriState.TRUE;
    }

    @Override
    public boolean paper$freezeLocked() {
        return this.paper$freezeLocked;
    }

    @Override
    public void paper$setFreezeLocked(boolean freezeLocked) {
        this.paper$freezeLocked = freezeLocked;
    }

    @Override
    public EntityRemoveEvent.@Nullable Cause paper$removeEventCause() {
        return this.paper$removeEventCause;
    }

    @Override
    public void paper$setRemoveEventCause(EntityRemoveEvent.@Nullable Cause cause) {
        this.paper$removeEventCause = cause;
    }

    @Override
    public int paper$totalEntityAge() {
        return this.paper$totalEntityAge;
    }

    @Override
    public void paper$setTotalEntityAge(int age) {
        this.paper$totalEntityAge = age;
    }

    @Override
    public @Nullable Vec3 paper$origin() {
        return this.paper$origin;
    }

    @Override
    public void paper$setOrigin(@Nullable Vec3 origin) {
        this.paper$origin = origin;
    }

    @Override
    public @Nullable UUID paper$originWorld() {
        return this.paper$originWorld;
    }

    @Override
    public void paper$setOriginWorld(@Nullable UUID originWorld) {
        this.paper$originWorld = originWorld;
    }

    @Override
    public boolean paper$spawnedViaMobSpawner() {
        return this.paper$spawnedViaMobSpawner;
    }

    @Override
    public void paper$setSpawnedViaMobSpawner(boolean spawnedViaMobSpawner) {
        this.paper$spawnedViaMobSpawner = spawnedViaMobSpawner;
    }

    @Override
    public CreatureSpawnEvent.@Nullable SpawnReason paper$spawnReason() {
        return this.paper$spawnReason;
    }

    @Override
    public void paper$setSpawnReason(CreatureSpawnEvent.@Nullable SpawnReason spawnReason) {
        this.paper$spawnReason = spawnReason;
    }
}
