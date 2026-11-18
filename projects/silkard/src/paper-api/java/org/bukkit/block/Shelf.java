package org.bukkit.block;

import org.bukkit.util.Vector; // Silkard - Spigot API compat
import org.jetbrains.annotations.NotNull; // Silkard - Spigot API compat
import io.papermc.paper.block.TileStateInventoryHolder;
import org.bukkit.inventory.ShelfInventory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Shelf extends TileStateInventoryHolder {

    @Override
    ShelfInventory getInventory();

    @Override
    ShelfInventory getSnapshotInventory();
    // Silkard start - Spigot API compat
    /**
     * Gets the appropriate slot based on a vector relative to this block.<br>
     * Will return -1 if the given vector is not within the bounds of any slot.
     * <p>
     * The supplied vector should only contain components with values between 0.0
     * and 1.0, inclusive.
     *
     * @param position a vector relative to this block
     * @return the slot under the given vector or -1
     */
    int getSlot(@NotNull Vector position);
    // Silkard end

}
