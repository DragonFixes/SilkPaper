package com.mohistmc.silkard.mixin.paper.paper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.papermc.paper.adventure.ChatProcessor;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Runs player chat through Paper's {@link ChatProcessor} so plugins receive the chat events.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinPaperServerGamePacketListenerImpl {

    // Without plugin chat listeners the vanilla broadcast runs unchanged, which keeps Fabric's chat
    // events (fired from PlayerList) in charge of the message.
    // TODO Silkard - with plugin chat listeners, viewer-aware rendering sends to each player directly and
    //  bypasses Fabric's PlayerList chat hooks; the unchanged-message path still goes through PlayerList
    @WrapOperation(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"))
    private void paper$processChat(final PlayerList playerList, final PlayerChatMessage message, final ServerPlayer sender, final ChatType.Bound chatType, final Operation<Void> original) {
        if (!paper$hasChatListeners()) {
            original.call(playerList, message, sender, chatType);
            return;
        }
        final MinecraftServer server = sender.level().getServer();
        new ChatProcessor(server, sender, message, !server.isSameThread()).process();
    }

    @Unique
    private static boolean paper$hasChatListeners() {
        return paper$listening(AsyncChatEvent.getHandlerList()) || paper$listening(ChatEvent.getHandlerList())
            || paper$listening(AsyncPlayerChatEvent.getHandlerList()) || paper$listening(PlayerChatEvent.getHandlerList());
    }

    @Unique
    private static boolean paper$listening(final HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }
}
