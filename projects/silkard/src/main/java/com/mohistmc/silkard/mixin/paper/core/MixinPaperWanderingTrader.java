package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper's wandering trader potion and milk switches. */
@Mixin(net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader.class)
public abstract class MixinPaperWanderingTrader implements PaperMobExtras.WanderingTrader {
    // TODO Silkard - the trader's UseItemGoal conditions do not consult these switches yet
    @Unique private boolean paper$canDrinkPotion = true;
    @Unique private boolean paper$canDrinkMilk = true;

    @Override
    public boolean paper$canDrinkPotion() {
        return this.paper$canDrinkPotion;
    }

    @Override
    public void paper$setCanDrinkPotion(boolean canDrinkPotion) {
        this.paper$canDrinkPotion = canDrinkPotion;
    }

    @Override
    public boolean paper$canDrinkMilk() {
        return this.paper$canDrinkMilk;
    }

    @Override
    public void paper$setCanDrinkMilk(boolean canDrinkMilk) {
        this.paper$canDrinkMilk = canDrinkMilk;
    }
}
