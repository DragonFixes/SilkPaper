package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.entity.ExperienceOrb.SpawnReason;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's experience orb origin: the entity that triggered the drop, the entity it came from and
 * why it spawned, saved under Paper's {@code Paper.ExpData} compound.
 * TODO Silkard - vanilla drop paths do not fill these yet; orbs spawned there report UNKNOWN.
 */
@Mixin(net.minecraft.world.entity.ExperienceOrb.class)
public abstract class MixinPaperExperienceOrb implements PaperMobExtras.ExperienceOrbData {

    @Unique private @Nullable UUID paper$triggerEntityId;
    @Unique private @Nullable UUID paper$sourceEntityId;
    @Unique private SpawnReason paper$spawnReason = SpawnReason.UNKNOWN;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveExpData(ValueOutput output, CallbackInfo ci) {
        if (this.paper$triggerEntityId == null && this.paper$sourceEntityId == null && this.paper$spawnReason == SpawnReason.UNKNOWN) {
            return;
        }
        ValueOutput data = output.child("Paper.ExpData");
        if (this.paper$sourceEntityId != null) {
            data.store("source", UUIDUtil.CODEC, this.paper$sourceEntityId);
        }
        if (this.paper$triggerEntityId != null) {
            data.store("trigger", UUIDUtil.CODEC, this.paper$triggerEntityId);
        }
        data.putString("reason", this.paper$spawnReason.name());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$loadExpData(ValueInput input, CallbackInfo ci) {
        input.child("Paper.ExpData").ifPresent(data -> {
            this.paper$sourceEntityId = data.read("source", UUIDUtil.CODEC).orElse(null);
            this.paper$triggerEntityId = data.read("trigger", UUIDUtil.CODEC).orElse(null);
            data.getString("reason").ifPresent(name -> {
                try {
                    this.paper$spawnReason = SpawnReason.valueOf(name);
                } catch (IllegalArgumentException ignored) {
                    this.paper$spawnReason = SpawnReason.UNKNOWN;
                }
            });
        });
    }

    @Override
    public @Nullable UUID paper$triggerEntityId() {
        return this.paper$triggerEntityId;
    }

    @Override
    public void paper$setTriggerEntityId(@Nullable UUID id) {
        this.paper$triggerEntityId = id;
    }

    @Override
    public @Nullable UUID paper$sourceEntityId() {
        return this.paper$sourceEntityId;
    }

    @Override
    public void paper$setSourceEntityId(@Nullable UUID id) {
        this.paper$sourceEntityId = id;
    }

    @Override
    public SpawnReason paper$spawnReason() {
        return this.paper$spawnReason;
    }

    @Override
    public void paper$setSpawnReason(SpawnReason reason) {
        this.paper$spawnReason = reason;
    }
}
