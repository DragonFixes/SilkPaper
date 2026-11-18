package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Purpur's player-spawned endermite flag. */
@Mixin(net.minecraft.world.entity.monster.Endermite.class)
public abstract class MixinPaperEndermite implements PaperMobExtras.Endermite {
    @Unique private boolean paper$playerSpawned;

    @Override
    public boolean paper$playerSpawned() {
        return this.paper$playerSpawned;
    }

    @Override
    public void paper$setPlayerSpawned(boolean playerSpawned) {
        this.paper$playerSpawned = playerSpawned;
    }
}
