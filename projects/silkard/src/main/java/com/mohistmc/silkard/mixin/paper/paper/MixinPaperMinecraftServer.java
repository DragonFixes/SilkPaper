package com.mohistmc.silkard.mixin.paper.paper;

import ca.spottedleaf.common.time.TickData;
import ca.spottedleaf.common.time.TickTime;
import com.mohistmc.silkard.injected.paper.paper.PaperMinecraftServerAccess;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's tick time statistics (/mspt, TPS bar, Gale 5-second TPS) and main-thread scheduling.
 */
@Mixin(MinecraftServer.class)
public abstract class MixinPaperMinecraftServer implements PaperMinecraftServerAccess {

    @Unique private final TickData paper$tickTimes5s = new TickData(TimeUnit.SECONDS.toNanos(5L));
    @Unique private final TickData paper$tickTimes10s = new TickData(TimeUnit.SECONDS.toNanos(10L));
    @Unique private final TickData paper$tickTimes1m = new TickData(TimeUnit.MINUTES.toNanos(1L));
    @Unique private final TickData paper$tickTimes5m = new TickData(TimeUnit.MINUTES.toNanos(5L));
    @Unique private final TickData paper$tickTimes15m = new TickData(TimeUnit.MINUTES.toNanos(15L));
    @Unique private final Object paper$statsLock = new Object();
    @Unique private long paper$lastTickStart;
    @Unique private long paper$currentTickStart;
    @Unique private boolean paper$isIteratingOverLevels;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void paper$startTick(final BooleanSupplier haveTime, final CallbackInfo ci) {
        this.paper$currentTickStart = System.nanoTime();
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void paper$endTick(final BooleanSupplier haveTime, final CallbackInfo ci) {
        final long end = System.nanoTime();
        final long start = this.paper$currentTickStart;
        final TickTime time = new TickTime(this.paper$lastTickStart == 0L ? -1L : this.paper$lastTickStart, start, start, 0L, end, 0L, 0L, 0L, false);
        this.paper$lastTickStart = start;
        synchronized (this.paper$statsLock) {
            this.paper$tickTimes5s.addDataFrom(time);
            this.paper$tickTimes10s.addDataFrom(time);
            this.paper$tickTimes1m.addDataFrom(time);
            this.paper$tickTimes5m.addDataFrom(time);
            this.paper$tickTimes15m.addDataFrom(time);
        }
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void paper$startLevels(final BooleanSupplier haveTime, final CallbackInfo ci) {
        this.paper$isIteratingOverLevels = true;
    }

    @Inject(method = "tickChildren", at = @At("RETURN"))
    private void paper$endLevels(final BooleanSupplier haveTime, final CallbackInfo ci) {
        this.paper$isIteratingOverLevels = false;
    }

    @Override
    public TickData paper$tickTimes5s() {
        return this.paper$tickTimes5s;
    }

    @Override
    public TickData paper$tickTimes10s() {
        return this.paper$tickTimes10s;
    }

    @Override
    public TickData paper$tickTimes1m() {
        return this.paper$tickTimes1m;
    }

    @Override
    public boolean paper$isIteratingOverLevels() {
        return this.paper$isIteratingOverLevels;
    }

    @Unique
    private static double paper$tps(final TickData data, final long interval) {
        final Double tps = data.getTPSAverage(null, interval);
        return tps == null ? -1.0 : tps;
    }

    @Override
    public double[] getTPSIncluding5SecondsReadOnly() {
        final long interval = ((MinecraftServer) (Object) this).tickRateManager().nanosecondsPerTick();
        synchronized (this.paper$statsLock) {
            return new double[] {
                paper$tps(this.paper$tickTimes5s, interval),
                paper$tps(this.paper$tickTimes1m, interval),
                paper$tps(this.paper$tickTimes5m, interval),
                paper$tps(this.paper$tickTimes15m, interval)
            };
        }
    }

    @Override
    public void scheduleOnMain(final Runnable task) {
        ((MinecraftServer) (Object) this).execute(task);
    }
}
