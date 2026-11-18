package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperServerLevelAccess;
import com.mohistmc.silkard.paper.paper.TickTimes;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Leaf's per-level tick times, used by /mspt and the TPS bar.
 */
@Mixin(ServerLevel.class)
public abstract class MixinPaperServerLevel implements PaperServerLevelAccess {

    @Unique private final TickTimes paper$tickTimes5s = new TickTimes(100);
    @Unique private final TickTimes paper$tickTimes10s = new TickTimes(200);
    @Unique private final TickTimes paper$tickTimes1m = new TickTimes(1200);
    @Unique private long paper$tickStart;
    @Unique private int paper$tickIndex;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void paper$startTick(final BooleanSupplier haveTime, final CallbackInfo ci) {
        this.paper$tickStart = System.nanoTime();
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("RETURN"))
    private void paper$endTick(final BooleanSupplier haveTime, final CallbackInfo ci) {
        final long duration = System.nanoTime() - this.paper$tickStart;
        final int index = this.paper$tickIndex++;
        this.paper$tickTimes5s.add(index, duration);
        this.paper$tickTimes10s.add(index, duration);
        this.paper$tickTimes1m.add(index, duration);
    }

    @Override
    public TickTimes paper$tickTimes5s() {
        return this.paper$tickTimes5s;
    }

    @Override
    public TickTimes paper$tickTimes10s() {
        return this.paper$tickTimes10s;
    }

    @Override
    public TickTimes paper$tickTimes1m() {
        return this.paper$tickTimes1m;
    }
}
