package com.mohistmc.silkard.paper.core;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.inventory.ItemStack;

/**
 * One slot's change in an {@link EntityEquipmentChangedEvent}. Listeners get copies, so editing a
 * returned stack does not change the event or the entity.
 */
public record PaperEquipmentChange(ItemStack oldItem, ItemStack newItem) implements EntityEquipmentChangedEvent.EquipmentChange {

    @Override
    public ItemStack oldItem() {
        return this.oldItem.clone();
    }

    @Override
    public ItemStack newItem() {
        return this.newItem.clone();
    }
}
