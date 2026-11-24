package com.mohistmc.silkard.gametest;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunctionLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;

/**
 * Registry of the Java gameplay tests. Each function here needs a matching
 * {@code gametests/packs/silkpaper-tests/data/silkpaper/test_instance/<name>.json}.
 * <p>
 * Coordinates are relative to the test structure ({@code silkpaper:platform}: 9x6x9, stone floor at y=0).
 */
public final class SilkTestFunctions implements TestFunctionLoader {

    static ResourceKey<Consumer<GameTestHelper>> key(final String name) {
        return ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("silkpaper", name));
    }

    @Override
    public void load(final BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> register) {
        register.accept(key("hopper_moves_item_into_chest"), SilkTestFunctions::hopperMovesItemIntoChest);
    }

    // A hopper above a chest pulls a dropped item in and pushes it down into the chest.
    private static void hopperMovesItemIntoChest(final GameTestHelper helper) {
        final BlockPos chest = new BlockPos(4, 1, 4);
        final BlockPos hopper = chest.above();
        helper.setBlock(chest, Blocks.CHEST);
        helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.spawnItem(Items.DIAMOND, hopper.above());
        helper.succeedWhen(() -> helper.assertContainerContains(chest, Items.DIAMOND));
    }
}
