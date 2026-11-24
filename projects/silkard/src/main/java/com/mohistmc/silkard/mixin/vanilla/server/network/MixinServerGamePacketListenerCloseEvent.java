package com.mohistmc.silkard.mixin.vanilla.server.network;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CraftBukkit - a player closing their container fires InventoryCloseEvent with reason PLAYER. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerCloseEvent {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleContainerClose", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"))
    private void silkard$fireCloseEvent(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        if (this.player.silkard$isImmobile()) {
            ci.cancel(); // CraftBukkit - frozen players keep their container
            return;
        }
        CraftEventFactory.handleInventoryCloseEvent(this.player, InventoryCloseEvent.Reason.PLAYER); // CraftBukkit // Paper
    }
}
