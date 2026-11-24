package com.mohistmc.silkard.mixin.vanilla.server.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.world.level.timers.TimerQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mgazul
 * @date 2026/6/8
 */
@Mixin(ScheduleCommand.class)
public class MixinScheduleCommand {

    // CraftBukkit - SPIGOT-6667: Use world specific function timer. The trigger time is the
    // source level's game time, so the entry has to go to the queue that level ticks.
    @Redirect(method = "schedule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"))
    private static TimerQueue<MinecraftServer> silkard$scheduleQueue(MinecraftServer server, CommandSourceStack source, Pair<?, ?> callback, int time, boolean replace) {
        return source.getLevel().getScheduledEvents();
    }

    // Paper - Make schedule command per-world
    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"))
    private static TimerQueue<MinecraftServer> silkard$removeQueue(MinecraftServer server, CommandSourceStack source, Identifier id) {
        return source.getLevel().getScheduledEvents();
    }

    // Paper - Make schedule command per-world (the SUGGEST_SCHEDULE suggestion provider)
    @Redirect(method = "lambda$static$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"))
    private static TimerQueue<MinecraftServer> silkard$suggestQueue(MinecraftServer server, CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return context.getSource().getLevel().getScheduledEvents();
    }
}
