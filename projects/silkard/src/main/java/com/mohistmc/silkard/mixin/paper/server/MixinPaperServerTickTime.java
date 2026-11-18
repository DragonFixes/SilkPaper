package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.paper.server.PaperServerHooks;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Measures each server tick for Gale's last-tick-time API ({@code Server#getLastTickProperTime}).
 */
@Mixin(MinecraftServer.class)
public abstract class MixinPaperServerTickTime {

    @Unique private long silkard$tickStartNanos;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void silkard$startTickTimer(final BooleanSupplier haveTime, final CallbackInfo ci) {
        this.silkard$tickStartNanos = System.nanoTime();
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void silkard$stopTickTimer(final BooleanSupplier haveTime, final CallbackInfo ci) {
        PaperServerHooks.recordTickTime(System.nanoTime() - this.silkard$tickStartNanos);
    }
}
