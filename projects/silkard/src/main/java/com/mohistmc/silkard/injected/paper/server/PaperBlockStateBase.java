package com.mohistmc.silkard.injected.paper.server;

import org.bukkit.Material;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

/**
 * Paper's Bukkit views of a block state, injected into {@code BlockBehaviour.BlockStateBase}.
 */
public interface PaperBlockStateBase {

    /**
     * @return a fresh copy of this state's Bukkit block data
     */
    default CraftBlockData asBlockData() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * @return the Bukkit material of this state's block
     */
    default Material getBukkitMaterial() {
        throw new IllegalStateException("Not implemented");
    }
}
