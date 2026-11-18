package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Purpur's rabid wolf flag. */
@Mixin(net.minecraft.world.entity.animal.wolf.Wolf.class)
public abstract class MixinPaperWolf implements PaperMobExtras.Wolf {
    // TODO Silkard - Purpur's rabid wolf behavior (attacking, infection, cure) is not ported; this is plain state
    @Unique private boolean paper$rabid;

    @Override
    public boolean paper$rabid() {
        return this.paper$rabid;
    }

    @Override
    public void paper$setRabid(boolean rabid) {
        this.paper$rabid = rabid;
    }
}
