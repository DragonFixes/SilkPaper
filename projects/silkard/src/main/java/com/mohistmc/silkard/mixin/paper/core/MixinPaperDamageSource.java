package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import net.kyori.adventure.pointer.Pointers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** The Adventure damage context a damage source was built with through the API. */
@Mixin(net.minecraft.world.damagesource.DamageSource.class)
public abstract class MixinPaperDamageSource implements PaperItemExtras.DamageSource {
    @Unique private Pointers paper$damageContext = Pointers.empty();

    @Override
    public Pointers paper$damageContext() {
        return this.paper$damageContext;
    }

    @Override
    public void paper$setDamageContext(Pointers context) {
        this.paper$damageContext = context;
    }
}
