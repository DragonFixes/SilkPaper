package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper's falling block auto-expire switch. */
@Mixin(net.minecraft.world.entity.item.FallingBlockEntity.class)
public abstract class MixinPaperFallingBlock implements PaperMobExtras.FallingBlock {
    // TODO Silkard - the falling block's 600 tick expiry does not consult this switch yet
    @Unique private boolean paper$autoExpire = true;

    @Override
    public boolean paper$autoExpire() {
        return this.paper$autoExpire;
    }

    @Override
    public void paper$setAutoExpire(boolean autoExpire) {
        this.paper$autoExpire = autoExpire;
    }
}
