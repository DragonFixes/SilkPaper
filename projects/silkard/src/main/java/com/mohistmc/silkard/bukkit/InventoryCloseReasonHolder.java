package com.mohistmc.silkard.bukkit;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Paper - Inventory close reason. Implemented on {@code ServerPlayer}: closes the open container
 * and reports {@code reason} in the InventoryCloseEvent that {@code closeContainer()} fires.
 */
public interface InventoryCloseReasonHolder {

    void silkard$closeContainer(InventoryCloseEvent.Reason reason);
}
