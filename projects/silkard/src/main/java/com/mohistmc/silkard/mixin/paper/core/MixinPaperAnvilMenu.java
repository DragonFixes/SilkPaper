package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper's anvil overrides.
 * TODO Silkard - the anvil result calculation does not consult these overrides yet; they are stored only.
 */
@Mixin(net.minecraft.world.inventory.AnvilMenu.class)
public abstract class MixinPaperAnvilMenu implements PaperItemExtras.AnvilMenu {
    @Unique private boolean paper$bypassEnchantmentLevelRestriction;
    @Unique private boolean paper$bypassCost;
    @Unique private boolean paper$canDoUnsafeEnchants;

    @Override
    public boolean paper$bypassEnchantmentLevelRestriction() {
        return this.paper$bypassEnchantmentLevelRestriction;
    }

    @Override
    public void paper$setBypassEnchantmentLevelRestriction(boolean bypass) {
        this.paper$bypassEnchantmentLevelRestriction = bypass;
    }

    @Override
    public boolean paper$bypassCost() {
        return this.paper$bypassCost;
    }

    @Override
    public void paper$setBypassCost(boolean bypass) {
        this.paper$bypassCost = bypass;
    }

    @Override
    public boolean paper$canDoUnsafeEnchants() {
        return this.paper$canDoUnsafeEnchants;
    }

    @Override
    public void paper$setCanDoUnsafeEnchants(boolean unsafe) {
        this.paper$canDoUnsafeEnchants = unsafe;
    }
}
