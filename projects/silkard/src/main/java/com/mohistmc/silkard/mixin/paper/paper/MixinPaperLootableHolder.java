package com.mohistmc.silkard.mixin.paper.paper;

import com.destroystokyo.paper.loottable.PaperLootableInventoryData;
import com.mohistmc.silkard.injected.paper.paper.PaperLootableHolderAccess;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Holds Paper's lootable inventory data on every lootable container.
 */
// TODO Silkard - the data is not saved with the container and loot tables are not refilled; Paper does both
//  from RandomizableContainer's default methods, which a mixin cannot inject into. Block state snapshots copy
//  the block entity through NBT, so lootable changes made through getState() are dropped even in memory
@Mixin({RandomizableContainerBlockEntity.class, AbstractMinecartContainer.class, AbstractChestBoat.class})
public abstract class MixinPaperLootableHolder implements PaperLootableHolderAccess {

    @Unique private final PaperLootableInventoryData paper$lootableData = new PaperLootableInventoryData();

    @Override
    public PaperLootableInventoryData paper$lootableData() {
        return this.paper$lootableData;
    }
}
