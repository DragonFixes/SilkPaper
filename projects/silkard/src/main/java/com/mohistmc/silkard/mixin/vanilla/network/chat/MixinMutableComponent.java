package com.mohistmc.silkard.mixin.vanilla.network.chat;

import io.papermc.paper.adventure.AdventureComponent;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableComponent.class)
public abstract class MixinMutableComponent {

    // Paper - make AdventureComponent equivalent: AdventureComponent.equals compares its converted
    // form, so the vanilla side has to do the same or equality (and with it ItemStack#isSimilar
    // on items named through the Adventure API) depends on which side is asked.
    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    private void silkard$equalsAdventure(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (o instanceof AdventureComponent adventureComponent) {
            cir.setReturnValue(((MutableComponent) (Object) this).equals(adventureComponent.deepConverted()));
        }
    }
}
