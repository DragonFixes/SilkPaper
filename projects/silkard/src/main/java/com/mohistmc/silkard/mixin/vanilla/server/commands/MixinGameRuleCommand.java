package com.mohistmc.silkard.mixin.vanilla.server.commands;

import com.mojang.brigadier.context.CommandContext;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mgazul
 * @date 2026/6/8
 */
@Mixin(GameRuleCommand.class)
public class MixinGameRuleCommand {

    // Paper - per-world game rules and event: the rule changes in the sender's world, after
    // WorldGameRuleChangeEvent, and the change is announced to that world only.
    @Redirect(method = "setRule", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;set(Lnet/minecraft/world/level/gamerules/GameRule;Ljava/lang/Object;Lnet/minecraft/server/MinecraftServer;)V"))
    private static <T> void silkard$setInSenderWorld(GameRules rules, GameRule<T> rule, T value, MinecraftServer server, @Local(argsOnly = true) CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CraftEventFactory.handleGameRuleSet(rule, value, source.getLevel(), source.getBukkitSender());
    }
}
