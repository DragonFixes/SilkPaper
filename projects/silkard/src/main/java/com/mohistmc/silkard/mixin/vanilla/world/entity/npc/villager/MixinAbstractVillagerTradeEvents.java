package com.mohistmc.silkard.mixin.vanilla.world.entity.npc.villager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.paper.core.PaperVillagers;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A completed villager trade counts a use and rewards experience only when its PlayerTradeEvent
 * (see {@link PaperVillagers#currentTrade}) leaves those on. Trades completed without an event
 * keep vanilla's behaviour.
 */
@Mixin(AbstractVillager.class)
public abstract class MixinAbstractVillagerTradeEvents {

    @WrapOperation(method = "notifyTrade",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;increaseUses()V"),
            require = 1)
    private void silkard$tradeUses(MerchantOffer offer, Operation<Void> original) {
        PlayerPurchaseEvent event = PaperVillagers.currentTrade;
        if (event == null || event.willIncreaseTradeUses()) {
            original.call(offer);
        }
    }

    @WrapOperation(method = "notifyTrade",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/AbstractVillager;rewardTradeXp(Lnet/minecraft/world/item/trading/MerchantOffer;)V"),
            require = 1)
    private void silkard$tradeXp(AbstractVillager villager, MerchantOffer offer, Operation<Void> original) {
        PlayerPurchaseEvent event = PaperVillagers.currentTrade;
        if (event == null || event.isRewardingExp()) {
            original.call(villager, offer);
        }
    }
}
