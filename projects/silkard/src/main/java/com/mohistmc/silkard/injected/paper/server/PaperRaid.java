package com.mohistmc.silkard.injected.paper.server;

import org.bukkit.persistence.PersistentDataContainer;

/**
 * Paper's raid persistent data container, injected into {@code net.minecraft.world.entity.raid.Raid}.
 */
public interface PaperRaid {

    default PersistentDataContainer paper$persistentDataContainer() {
        throw new IllegalStateException("Not implemented");
    }
}
