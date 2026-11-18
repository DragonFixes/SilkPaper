package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Purpur's summoner of a built golem or wither, saved under Purpur's key. */
@Mixin({IronGolem.class, SnowGolem.class, CopperGolem.class, WitherBoss.class})
public abstract class MixinPaperSummonable implements PaperMobExtras.Summonable {

    @Unique private @Nullable UUID paper$summoner;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveSummoner(ValueOutput output, CallbackInfo ci) {
        if (this.paper$summoner != null) {
            output.store("Purpur.Summoner", UUIDUtil.CODEC, this.paper$summoner);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$loadSummoner(ValueInput input, CallbackInfo ci) {
        this.paper$summoner = input.read("Purpur.Summoner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public @Nullable UUID paper$summoner() {
        return this.paper$summoner;
    }

    @Override
    public void paper$setSummoner(@Nullable UUID summoner) {
        this.paper$summoner = summoner;
    }
}
