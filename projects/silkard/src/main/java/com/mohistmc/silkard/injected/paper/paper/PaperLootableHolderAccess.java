package com.mohistmc.silkard.injected.paper.paper;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;
import com.mohistmc.silkard.util.ContextStateException;

/**
 * The loot table refill state Paper adds to lootable containers (block entities, chest minecarts and boats).
 */
public interface PaperLootableHolderAccess {

    default PaperLootableInventoryData paper$lootableData() {
        throw new ContextStateException("Not implemented");
    }
}
