package com.mohistmc.silkard.mixin.vanilla.gametest;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.silkard.injected.world.level.gamerules.ContextGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Game rules are kept per world (CraftBukkit), so the {@code game_rules} test environment changes and
 * announces them for the level the test runs in, as Paper does.
 */
@Mixin(targets = "net.minecraft.gametest.framework.TestEnvironmentDefinition$SetGameRules")
public class MixinTestEnvironmentDefinition$SetGameRules {

    @Redirect(method = { "setup", "teardown" }, require = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;setAll(Lnet/minecraft/world/level/gamerules/GameRuleMap;Lnet/minecraft/server/MinecraftServer;)V"))
    private void silkpaper$setInLevel(GameRules rules, GameRuleMap map, MinecraftServer server, @Local(argsOnly = true) ServerLevel level) {
        ((ContextGameRules) rules).setAll(map, level);
    }
}
