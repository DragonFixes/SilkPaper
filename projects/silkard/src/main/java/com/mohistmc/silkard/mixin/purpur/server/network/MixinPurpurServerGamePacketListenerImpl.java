package com.mohistmc.silkard.mixin.purpur.server.network;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Purpur - AFK API. A player past the idle timeout becomes AFK; they are only kicked when
 * idle-timeout kick is on and they lack purpur.bypassIdleKick (PURPUR_FORCE_IDLE_KICK=true ignores
 * the permission).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinPurpurServerGamePacketListenerImpl {

    @Shadow public ServerPlayer player;

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=multiplayer.disconnect.idling")), require = 1, allow = 1)
    private boolean purpur$idleAfk(ServerGamePacketListenerImpl listener, Component reason) {
        this.player.paper$setAfk(true);
        if (!this.player.level().purpurConfig().idleTimeoutKick) return false;
        return Boolean.parseBoolean(System.getenv("PURPUR_FORCE_IDLE_KICK")) || !this.player.getBukkitEntity().hasPermission("purpur.bypassIdleKick");
    }
}
