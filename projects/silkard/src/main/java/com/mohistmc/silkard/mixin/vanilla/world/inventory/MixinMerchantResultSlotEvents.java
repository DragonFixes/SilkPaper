package com.mohistmc.silkard.mixin.vanilla.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mohistmc.silkard.injected.world.item.trading.ContextMerchantOffer;
import com.mohistmc.silkard.paper.core.PaperVillagers;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's PlayerTradeEvent (villagers and wandering traders) and PlayerPurchaseEvent (merchants
 * made by plugins) when a player takes a trade's result. A cancelled trade takes nothing and
 * resyncs the menu; otherwise the trade a listener set is the one completed, and its use count
 * and experience follow the event.
 */
@Mixin(MerchantResultSlot.class)
public abstract class MixinMerchantResultSlotEvents {

    // @formatter:off
    @Shadow @Final private MerchantContainer slots;
    @Shadow @Final private Merchant merchant;
    // @formatter:on

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true, require = 1)
    private void silkard$tradeEvent(Player player, ItemStack carried, CallbackInfo ci, @Share("tradeEvent") LocalRef<PlayerPurchaseEvent> eventRef) {
        MerchantOffer offer = this.slots.getActiveOffer();
        if (offer == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PlayerPurchaseEvent event;
        if (this.merchant instanceof AbstractVillager villager) {
            event = new PlayerTradeEvent(serverPlayer.getBukkitEntity(), (org.bukkit.entity.AbstractVillager) villager.getBukkitEntity(), ((ContextMerchantOffer) offer).asBukkit(), true, true);
        } else if (this.merchant instanceof CraftMerchantCustom.MinecraftMerchant custom) {
            event = new PlayerPurchaseEvent(serverPlayer.getBukkitEntity(), custom.getCraftMerchant(), ((ContextMerchantOffer) offer).asBukkit(), false, true);
        } else {
            return;
        }
        if (!event.callEvent()) {
            carried.setCount(0);
            player.containerMenu.sendAllDataToRemote();
            int level = this.merchant instanceof Villager villager ? villager.getVillagerData().level() : 1;
            serverPlayer.sendMerchantOffers(player.containerMenu.containerId, this.merchant.getOffers(), level, this.merchant.getVillagerXp(), this.merchant.showProgressBar(), this.merchant.canRestock());
            ci.cancel();
            return;
        }
        eventRef.set(event);
    }

    @ModifyExpressionValue(method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantContainer;getActiveOffer()Lnet/minecraft/world/item/trading/MerchantOffer;"),
            require = 1)
    private MerchantOffer silkard$tradeEventOffer(MerchantOffer offer, @Share("tradeEvent") LocalRef<PlayerPurchaseEvent> eventRef) {
        PlayerPurchaseEvent event = eventRef.get();
        return event == null ? offer : CraftMerchantRecipe.fromBukkit(event.getTrade()).toMinecraft();
    }

    @WrapOperation(method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V"),
            require = 1)
    private void silkard$processTrade(Merchant merchant, MerchantOffer offer, Operation<Void> original, @Share("tradeEvent") LocalRef<PlayerPurchaseEvent> eventRef) {
        PlayerPurchaseEvent event = eventRef.get();
        if (merchant instanceof CraftMerchantCustom.MinecraftMerchant custom) {
            custom.processTrade(offer, event);
            return;
        }
        PaperVillagers.currentTrade = event;
        try {
            original.call(merchant, offer);
        } finally {
            PaperVillagers.currentTrade = null;
        }
    }
}
