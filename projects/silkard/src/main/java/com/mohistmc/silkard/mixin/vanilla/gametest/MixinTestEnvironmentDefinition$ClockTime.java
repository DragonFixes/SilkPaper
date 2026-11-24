package com.mohistmc.silkard.mixin.vanilla.gametest;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Time is kept per world (CraftBukkit), so the {@code clock_time} test environment has to set the clock
 * of the level the test runs in. The server-wide clock it sets in vanilla never reaches that level.
 */
@Mixin(targets = "net.minecraft.gametest.framework.TestEnvironmentDefinition$ClockTime")
public class MixinTestEnvironmentDefinition$ClockTime {

    @ModifyExpressionValue(method = { "setup", "teardown" }, require = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private ServerClockManager silkpaper$levelClock(ServerClockManager original, @Local(argsOnly = true) ServerLevel level) {
        return level.clockManager();
    }
}
