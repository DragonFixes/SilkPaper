package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper's ender dragon podium override. */
@Mixin(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class)
public abstract class MixinPaperEnderDragon implements PaperMobExtras.EnderDragon {
    // TODO Silkard - the dragon's phases still path to the podium of the fight origin; the override is reported only
    @Unique private @Nullable BlockPos paper$podium;

    @Override
    public @Nullable BlockPos paper$podium() {
        return this.paper$podium;
    }

    @Override
    public void paper$setPodium(@Nullable BlockPos podium) {
        this.paper$podium = podium;
    }
}
