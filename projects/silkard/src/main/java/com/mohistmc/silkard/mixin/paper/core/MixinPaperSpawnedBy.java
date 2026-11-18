package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's record of the entity whose action spawned a firework or phantom.
 * TODO Silkard - vanilla spawn paths (firework use, phantom spawner) do not set it yet; only the API does.
 */
@Mixin({FireworkRocketEntity.class, Phantom.class})
public abstract class MixinPaperSpawnedBy implements PaperMobExtras.SpawnedBy {

    @Unique private @Nullable UUID paper$spawningEntity;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveSpawningEntity(ValueOutput output, CallbackInfo ci) {
        if (this.paper$spawningEntity != null) {
            output.store("Paper.SpawningEntity", UUIDUtil.CODEC, this.paper$spawningEntity);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$loadSpawningEntity(ValueInput input, CallbackInfo ci) {
        this.paper$spawningEntity = input.read("Paper.SpawningEntity", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public @Nullable UUID paper$spawningEntity() {
        return this.paper$spawningEntity;
    }

    @Override
    public void paper$setSpawningEntity(@Nullable UUID spawningEntity) {
        this.paper$spawningEntity = spawningEntity;
    }
}
