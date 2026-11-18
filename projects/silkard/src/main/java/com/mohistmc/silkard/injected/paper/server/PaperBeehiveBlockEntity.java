package com.mohistmc.silkard.injected.paper.server;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Paper's and Purpur's access to the bees stored in a hive, injected into {@code BeehiveBlockEntity}.
 */
public interface PaperBeehiveBlockEntity {

    /**
     * @return the live list of stored bees
     */
    default List<BeehiveBlockEntity.BeeData> getStored() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Removes every stored bee without releasing it.
     */
    default void clearBees() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Releases one stored bee.
     *
     * @return the released entities; empty if the bee could not leave
     */
    default List<Entity> releaseBee(BlockState state, BeehiveBlockEntity.BeeData data, BeehiveBlockEntity.BeeReleaseStatus status, boolean force) {
        throw new IllegalStateException("Not implemented");
    }
}
