package org.bukkit.craftbukkit.block;

<<<<<<< silkard
import com.mohistmc.silkard.bukkit.DoubleChestCombiner;
=======
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
>>>>>>> paper
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.inventory.Inventory;

public class CraftChest extends CraftLootable<ChestBlockEntity> implements Chest {

    public CraftChest(World world, ChestBlockEntity blockEntity, boolean useSnapshot) {
        super(world, blockEntity, useSnapshot);
    }

    protected CraftChest(CraftChest state, Location location) {
        super(state, location);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getBlockInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getBlockEntity());
    }

    @Override
    public Inventory getInventory() {
        CraftInventory inventory = (CraftInventory) this.getBlockInventory();
        if (!this.isPlaced() || this.isWorldGeneration()) {
            return inventory;
        }

        CraftWorld world = (CraftWorld) this.getWorld();

        ChestBlock block = this.block.getBlock() instanceof ChestBlock chestBlock ? chestBlock : (ChestBlock) Blocks.CHEST;
        MenuProvider provider = block.getMenuProvider(this.block, world.getHandle(), this.getPosition(), true);

<<<<<<< silkard
        if (nms instanceof DoubleChestCombiner.Dummy.DoubleInventory) {
            inventory = new CraftInventoryDoubleChest((DoubleChestCombiner.Dummy.DoubleInventory) nms);
=======
        if (provider instanceof CraftInventoryDoubleChest.Provider doubleChestProvider) {
            inventory = new CraftInventoryDoubleChest(doubleChestProvider);
>>>>>>> paper
        }
        return inventory;
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
        // Method mimics vanilla logic in ChestBlock and DoubleBlockCombiner when trying to open chest's container
        if (!isPlaced()) {
            return false;
        }

        LevelAccessor level = this.getWorldHandle();
        if (ChestBlock.isChestBlockedAt(level, this.getPosition())) {
            return true;
        }
        if (ChestBlock.getBlockType(this.block) == DoubleBlockCombiner.BlockType.SINGLE) {
            return false;
        }
        Direction direction = ChestBlock.getConnectedDirection(this.block);
        BlockPos neighborPos = this.getPosition().relative(direction);
        BlockState neighborState = level.getBlockStateIfLoaded(neighborPos);
        return neighborState != null
            && neighborState.is(this.block.getBlock())
            && ChestBlock.getBlockType(neighborState) != DoubleBlockCombiner.BlockType.SINGLE
            && ChestBlock.getConnectedDirection(neighborState) == direction.getOpposite()
            && ChestBlock.isChestBlockedAt(level, neighborPos);
>>>>>>> paper
    }

    @Override
    public CraftChest copy() {
        return new CraftChest(this, null);
    }

    @Override
    public CraftChest copy(Location location) {
        return new CraftChest(this, location);
    }
}
