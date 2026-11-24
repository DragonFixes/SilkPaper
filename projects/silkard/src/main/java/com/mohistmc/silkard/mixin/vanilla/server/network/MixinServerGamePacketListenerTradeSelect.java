package com.mohistmc.silkard.mixin.vanilla.server.network;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.MerchantMenu;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CraftBukkit - TradeSelectEvent: a cancelled selection resyncs the merchant menu instead. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerTradeSelect {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleSelectTrade", cancellable = true, require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"))
    private void silkard$tradeSelectEvent(ServerboundSelectTradePacket packet, CallbackInfo ci, @Local MerchantMenu menu) {
        if (CraftEventFactory.callTradeSelectEvent(packet.getItem(), menu).isCancelled()) {
            this.player.containerMenu.sendAllDataToRemote();
            ci.cancel();
        }
    }
}
