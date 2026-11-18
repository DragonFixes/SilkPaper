package org.bukkit.craftbukkit.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftBarrel extends CraftLootable<BarrelBlockEntity> implements Barrel {

    public CraftBarrel(World world, BarrelBlockEntity blockEntity, boolean useSnapshot) {
        super(world, blockEntity, useSnapshot);
    }

    protected CraftBarrel(CraftBarrel state, Location location) {
        super(state, location);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getBlockEntity());
    }

    @Override
    public void open() {
<<<<<<< silkard
        requirePlaced();
        if (!getTileEntity().openersCounter.silkard_opened()) {
            BlockState blockData = getTileEntity().getBlockState();
            boolean open = blockData.getValue(BarrelBlock.OPEN);
=======
        this.requirePlaced();
        if (!this.getBlockEntity().openersCounter.opened && this.getWorldHandle() instanceof net.minecraft.world.level.Level level) {
            BlockState block = this.getBlockEntity().getBlockState();
            int openCount = this.getBlockEntity().openersCounter.getOpenerCount();
>>>>>>> paper

            this.getBlockEntity().openersCounter.onOpenAPI(level, this.getPosition(), block);
            this.getBlockEntity().openersCounter.openerCountChangedAPI(level, this.getPosition(), block, openCount, openCount + 1);
        }
<<<<<<< silkard
        getTileEntity().openersCounter.silkard_opened(true);
=======
        this.getBlockEntity().openersCounter.opened = true;
>>>>>>> paper
    }

    @Override
    public void close() {
<<<<<<< silkard
        requirePlaced();
        if (getTileEntity().openersCounter.silkard_opened()) {
            BlockState blockData = getTileEntity().getBlockState();
            getTileEntity().updateBlockState(blockData, false);
            if (getWorldHandle() instanceof net.minecraft.world.level.Level) {
                getTileEntity().playSound(blockData, SoundEvents.BARREL_CLOSE);
            }
        }
        getTileEntity().openersCounter.silkard_opened(false);
=======
        this.requirePlaced();
        if (this.getBlockEntity().openersCounter.opened && this.getWorldHandle() instanceof net.minecraft.world.level.Level level) {
            BlockState block = this.getBlockEntity().getBlockState();
            int openCount = this.getBlockEntity().openersCounter.getOpenerCount();

            this.getBlockEntity().openersCounter.onCloseAPI(level, this.getPosition(), block);
            this.getBlockEntity().openersCounter.openerCountChangedAPI(level, this.getPosition(), block, openCount, 0);
        }
        this.getBlockEntity().openersCounter.opened = false;
>>>>>>> paper
    }

    @Override
    public CraftBarrel copy() {
        return new CraftBarrel(this, null);
    }

    @Override
    public CraftBarrel copy(Location location) {
        return new CraftBarrel(this, location);
    }

    // Paper start - More Lidded Block API
    @Override
    public boolean isOpen() {
        return getBlockEntity().openersCounter.opened;
    }
    // Paper end - More Lidded Block API
}
