package org.bukkit.craftbukkit.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.EnderChest;

public class CraftEnderChest extends CraftBlockEntityState<EnderChestBlockEntity> implements EnderChest {

    public CraftEnderChest(World world, EnderChestBlockEntity blockEntity, boolean useSnapshot) {
        super(world, blockEntity, useSnapshot);
    }

    protected CraftEnderChest(CraftEnderChest state, Location location) {
        super(state, location);
    }

    @Override
    public void open() {
<<<<<<< silkard
        requirePlaced();
        if (!getTileEntity().openersCounter.silkard_opened() && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            BlockState block = getTileEntity().getBlockState();
            int openCount = getTileEntity().openersCounter.getOpenerCount();
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
        if (getTileEntity().openersCounter.silkard_opened() && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            BlockState block = getTileEntity().getBlockState();
            int openCount = getTileEntity().openersCounter.getOpenerCount();
=======
        this.requirePlaced();
        if (this.getBlockEntity().openersCounter.opened && this.getWorldHandle() instanceof net.minecraft.world.level.Level level) {
            BlockState block = this.getBlockEntity().getBlockState();
            int openCount = this.getBlockEntity().openersCounter.getOpenerCount();
>>>>>>> paper

            this.getBlockEntity().openersCounter.onCloseAPI(level, this.getPosition(), block);
            this.getBlockEntity().openersCounter.openerCountChangedAPI(level, this.getPosition(), block, openCount, 0);
        }
<<<<<<< silkard
        getTileEntity().openersCounter.silkard_opened(false);
=======
        this.getBlockEntity().openersCounter.opened = false;
    }

    @Override
    public boolean isOpen() {
        return this.getBlockEntity().openersCounter.opened;
    }

    @Override
    public boolean isBlocked() {
        // Uses the same logic as EnderChestBlock's check for opening container
        final BlockPos abovePos = this.getPosition().above();
        return this.isPlaced() && this.getWorldHandle().getBlockState(abovePos).isRedstoneConductor(this.getWorldHandle(), abovePos);
>>>>>>> paper
    }

    @Override
    public CraftEnderChest copy() {
        return new CraftEnderChest(this, null);
    }

    @Override
    public CraftEnderChest copy(Location location) {
        return new CraftEnderChest(this, location);
    }
}
