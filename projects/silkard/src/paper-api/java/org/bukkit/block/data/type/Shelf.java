package org.bukkit.block.data.type;

import org.jetbrains.annotations.NotNull; // Silkard - Spigot API compat
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.SideChaining;
import org.bukkit.block.data.Waterlogged;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Shelf extends Directional, SideChaining, Waterlogged, Powerable {
    // Silkard start - Spigot API compat
    /**
     * Represents the state of the shelf's side chain.
     */
    public enum SideChain {

        /**
         * Chain on the left.
         */
        LEFT,
        /**
         * Chain in the center.
         */
        CENTER,
        /**
         * Chain on the right.
         */
        RIGHT,
        /**
         * Chain not connected.
         */
        UNCONNECTED;
    }
    // Silkard end

    // Silkard start - Spigot API compat
    /**
     * Sets the value of the 'side_chain' property.
     *
     * @param sideChain the new 'side_chain' value
     */
    void setSideChain(@NotNull SideChain sideChain);
    // Silkard end

}
