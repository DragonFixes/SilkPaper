package com.mohistmc.silkard.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public final class SilkardCommands {

    private SilkardCommands() {
    }

    public static void registerVanilla(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher.getRoot().getChild("unban") == null) {
            UnbanCommand.register(dispatcher);
        }
        if (dispatcher.getRoot().getChild("unban-ip") == null) {
            UnBanIpCommand.register(dispatcher);
        }
        registerPurpur(dispatcher);
    }

    // Purpur registers these from its Commands constructor patch.
    private static void registerPurpur(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher.getRoot().getChild("tpsbar") != null) {
            return;
        }
        org.purpurmc.purpur.command.CreditsCommand.register(dispatcher);
        org.purpurmc.purpur.command.DemoCommand.register(dispatcher);
        org.purpurmc.purpur.command.PingCommand.register(dispatcher);
        org.purpurmc.purpur.command.UptimeCommand.register(dispatcher);
        org.purpurmc.purpur.command.TPSBarCommand.register(dispatcher);
        org.purpurmc.purpur.command.CompassCommand.register(dispatcher);
        org.purpurmc.purpur.command.RamBarCommand.register(dispatcher);
        org.purpurmc.purpur.command.RamCommand.register(dispatcher);
    }
}
