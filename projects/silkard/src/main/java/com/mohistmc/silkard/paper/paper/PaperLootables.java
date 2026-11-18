package com.mohistmc.silkard.paper.paper;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;
import com.mohistmc.silkard.injected.paper.paper.PaperLootableHolderAccess;
import java.util.Objects;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.loot.LootableInventory;
import org.jspecify.annotations.Nullable;

/**
 * The loot table members Paper adds to {@link RandomizableContainer} and {@link ContainerEntity}.
 */
public final class PaperLootables {

    private PaperLootables() {
    }

    /** Paper's lootable data of {@code holder}, or {@code null} when it is not a vanilla lootable container. */
    public static @Nullable PaperLootableInventoryData lootableData(final Object holder) {
        return holder instanceof final PaperLootableHolderAccess access ? access.paper$lootableData() : null;
    }

    public static LootableInventory getLootableInventory(final RandomizableContainer container) {
        return (LootableInventory) CraftBlock.at(Objects.requireNonNull(container.getLevel()), container.getBlockPos()).getState(false);
    }

    public static LootableInventory getLootableInventory(final ContainerEntity container) {
        return (LootableInventory) ((Entity) container).getBukkitEntity();
    }
}
