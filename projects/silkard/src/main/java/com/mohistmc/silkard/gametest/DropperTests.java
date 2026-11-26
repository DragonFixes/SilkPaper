package com.mohistmc.silkard.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

/**
 * A powered dropper spits its item out into whatever non-container block it faces, water and
 * bubble columns included. Layout: a 3-high water column at x=4, z=4 (y=2..4) over {@code floor},
 * walled in with glass, and a dropper either beside the bottom water block facing east or on top of
 * the column facing down.
 * Coordinates are relative to {@code silkpaper:platform} (stone floor at y=0).
 */
public final class DropperTests {

    private static final BlockPos FLOOR = new BlockPos(4, 1, 4);
    private static final BlockPos SIDE_DROPPER = new BlockPos(3, 2, 4);
    private static final BlockPos SIDE_POWER = new BlockPos(2, 2, 4);
    private static final BlockPos TOP_DROPPER = new BlockPos(4, 5, 4);
    private static final BlockPos TOP_POWER = new BlockPos(3, 5, 4);

    private DropperTests() {
    }

    public static void dropsIntoUpColumn(final GameTestHelper helper) {
        dropsInto(helper, Blocks.SOUL_SAND, true, false);
    }

    public static void dropsIntoDownColumn(final GameTestHelper helper) {
        dropsInto(helper, Blocks.MAGMA_BLOCK, true, false);
    }

    public static void dropsIntoWater(final GameTestHelper helper) {
        dropsInto(helper, Blocks.STONE, false, false);
    }

    public static void dropsDownIntoUpColumn(final GameTestHelper helper) {
        dropsInto(helper, Blocks.SOUL_SAND, true, true);
    }

    public static void dropsDownIntoDownColumn(final GameTestHelper helper) {
        dropsInto(helper, Blocks.MAGMA_BLOCK, true, true);
    }

    private static void dropsInto(final GameTestHelper helper, final Block floor, final boolean column, final boolean fromTop) {
        final BlockPos dropperPos = fromTop ? TOP_DROPPER : SIDE_DROPPER;
        final BlockPos powerPos = fromTop ? TOP_POWER : SIDE_POWER;
        helper.setBlock(FLOOR, floor);
        for (int y = 2; y <= 4; y++) {
            helper.setBlock(new BlockPos(5, y, 4), Blocks.GLASS);
            helper.setBlock(new BlockPos(4, y, 3), Blocks.GLASS);
            helper.setBlock(new BlockPos(4, y, 5), Blocks.GLASS);
            if (fromTop || y > 2) {
                helper.setBlock(new BlockPos(3, y, 4), Blocks.GLASS);
            }
        }
        helper.setBlock(dropperPos, Blocks.DROPPER.defaultBlockState().setValue(DispenserBlock.FACING, fromTop ? Direction.DOWN : Direction.EAST));
        for (int y = 2; y <= 4; y++) {
            helper.setBlock(new BlockPos(4, y, 4), Blocks.WATER);
        }
        helper.getBlockEntity(dropperPos, DispenserBlockEntity.class).setItem(0, new ItemStack(Items.DIAMOND));

        // Bubble columns grow from soul sand / magma scheduled ticks (20 ticks); power the dropper once the column is there.
        helper.runAfterDelay(30, () -> {
            if (column) {
                helper.assertBlockPresent(Blocks.BUBBLE_COLUMN, new BlockPos(4, 2, 4));
                helper.assertBlockPresent(Blocks.BUBBLE_COLUMN, new BlockPos(4, 4, 4));
            }
            helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);
        });
        helper.runAfterDelay(40, () -> {
            final DispenserBlockEntity dropper = helper.getBlockEntity(dropperPos, DispenserBlockEntity.class);
            helper.assertTrue(dropper.isEmpty(), "dropper still holds its item");
            int diamonds = 0;
            for (final ItemEntity item : helper.getEntities(EntityTypes.ITEM)) {
                if (item.getItem().is(Items.DIAMOND)) {
                    diamonds += item.getItem().getCount();
                }
            }
            helper.assertTrue(diamonds == 1, "expected 1 dropped diamond, found " + diamonds);
            helper.succeed();
        });
    }
}
