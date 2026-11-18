package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Shelf;
import org.bukkit.craftbukkit.inventory.CraftInventoryShelf;
import org.bukkit.inventory.ShelfInventory;

public class CraftShelf extends CraftBlockEntityState<ShelfBlockEntity> implements Shelf {

    public CraftShelf(World world, ShelfBlockEntity blockEntity, boolean useSnapshot) {
        super(world, blockEntity, useSnapshot);
    }

    protected CraftShelf(CraftShelf state, Location location) {
        super(state, location);
    }

    @Override
    public ShelfInventory getSnapshotInventory() {
        return new CraftInventoryShelf(this.getSnapshot());
    }

    @Override
    public ShelfInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryShelf(this.getBlockEntity());
    }

    @Override
    public CraftBlockEntityState<ShelfBlockEntity> copy() {
        return new CraftShelf(this, null);
    }

    @Override
    public CraftBlockEntityState<ShelfBlockEntity> copy(final Location location) {
        return new CraftShelf(this, location);
    }

    @Override
    public int getSlot(org.bukkit.util.Vector clickVector) {
        final org.bukkit.block.BlockFace facing = ((org.bukkit.block.data.Directional) this.getBlockData()).getFacing();

        final net.minecraft.world.phys.Vec2 faceVector;
        switch (facing) {
            case NORTH -> faceVector = new net.minecraft.world.phys.Vec2((float) (1.0F - clickVector.getX()), (float) clickVector.getY());
            case SOUTH -> faceVector = new net.minecraft.world.phys.Vec2((float) clickVector.getX(), (float) clickVector.getY());
            case WEST -> faceVector = new net.minecraft.world.phys.Vec2((float) clickVector.getZ(), (float) clickVector.getY());
            case EAST -> faceVector = new net.minecraft.world.phys.Vec2((float) (1.0F - clickVector.getZ()), (float) clickVector.getY());
            default -> {
                return -1;
            }
        }

        final int row = net.minecraft.world.level.block.SelectableSlotContainer.getSection(1.0F - faceVector.y, 1);
        final int column = net.minecraft.world.level.block.SelectableSlotContainer.getSection(faceVector.x, 3);
        return column + row * 3;
    }
}