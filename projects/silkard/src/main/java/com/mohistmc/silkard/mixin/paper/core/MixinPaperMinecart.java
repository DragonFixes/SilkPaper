package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.kyori.adventure.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper's minecart friction override. */
@Mixin(net.minecraft.world.entity.vehicle.minecart.AbstractMinecart.class)
public abstract class MixinPaperMinecart implements PaperMobExtras.Minecart {
    // TODO Silkard - minecart movement does not apply Paper's friction override yet
    @Unique private TriState paper$frictionState = TriState.NOT_SET;

    @Override
    public TriState paper$frictionState() {
        return this.paper$frictionState;
    }

    @Override
    public void paper$setFrictionState(TriState state) {
        this.paper$frictionState = state;
    }
}
