package com.mohistmc.silkard.paper.core;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Static counterparts of villager and merchant helpers that Paper adds to vanilla classes.
 */
public final class PaperVillagers {

    /**
     * The PlayerTradeEvent of the villager trade being completed, set only while
     * {@code Merchant.notifyTrade} runs for it, so the villager can skip the use count and
     * experience a listener turned off. Trades only complete on the server thread.
     */
    public static io.papermc.paper.event.player.@org.jspecify.annotations.Nullable PlayerPurchaseEvent currentTrade;

    private PaperVillagers() {
    }

    /** Replaces the merchant's offers with a freshly generated set. */
    public static void resetOffers(AbstractVillager merchant) {
        merchant.offers = new MerchantOffers();
        if (merchant.level() instanceof ServerLevel level) {
            merchant.updateTrades(level);
        }
    }

    /**
     * Unlocks up to {@code amount} new trades for the villager's current level. Vanilla unlocks a
     * fixed number per call, so trades are generated until enough were added and any surplus from
     * the last batch is dropped.
     *
     * @return whether at least one trade was added
     */
    public static boolean addTrades(Villager villager, ServerLevel level, int amount) {
        MerchantOffers offers = villager.getOffers();
        int start = offers.size();
        int target = start + amount;
        while (offers.size() < target) {
            int before = offers.size();
            villager.updateTrades(level);
            if (offers.size() == before) {
                break;
            }
        }
        while (offers.size() > target) {
            offers.removeLast();
        }
        return offers.size() > start;
    }

    /** The gossip values one villager holds about one entity, as Paper's reputation object. */
    public static Reputation asReputation(GossipContainer.EntityGossips gossips) {
        Reputation reputation = new Reputation();
        for (ReputationType type : ReputationType.values()) {
            int value = gossips.getRawValue(GossipType.valueOf(type.name()));
            if (value != 0) {
                reputation.setReputation(type, value);
            }
        }
        return reputation;
    }

    /** Overwrites the gossip values with those set in {@code reputation}. */
    public static void assignFromReputation(GossipContainer.EntityGossips gossips, Reputation reputation) {
        for (ReputationType type : ReputationType.values()) {
            if (reputation.hasReputationSet(type)) {
                gossips.setRawValue(GossipType.valueOf(type.name()), reputation.getReputation(type));
            }
        }
    }
}
