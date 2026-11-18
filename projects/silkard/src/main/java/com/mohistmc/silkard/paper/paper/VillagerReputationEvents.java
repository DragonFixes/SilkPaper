package com.mohistmc.silkard.paper.paper;

import java.util.UUID;
import net.minecraft.world.entity.npc.villager.Villager;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.VillagerReputationChangeEvent;

/**
 * Fires Spigot's {@link VillagerReputationChangeEvent} for Silkard's gossip hooks.
 */
public final class VillagerReputationEvents {

    private VillagerReputationEvents() {
    }

    public static VillagerReputationChangeEvent call(final Villager villager, final UUID targetUuid, final org.bukkit.entity.Villager.ReputationEvent reason,
                                                     final org.bukkit.entity.Villager.ReputationType reputationType, final int oldValue, final int newValue, final int maxValue) {
        final VillagerReputationChangeEvent event = new VillagerReputationChangeEvent((org.bukkit.entity.Villager) villager.getBukkitEntity(), targetUuid, reason, reputationType, oldValue, newValue, maxValue);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
