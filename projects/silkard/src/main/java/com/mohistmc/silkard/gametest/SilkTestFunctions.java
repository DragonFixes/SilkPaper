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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;

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
        register.accept(key("redstone_dust_lights_lamp"), SilkTestFunctions::redstoneDustLightsLamp);
        register.accept(key("selector_nbt_filters_entities"), SilkTestFunctions::selectorNbtFiltersEntities);
        register.accept(key("selector_nbt_matrix"), NbtTests::matrix);
        register.accept(key("selector_world_scan_matrix"), SelectorTests::matrix);
        register.accept(key("selector_tag_matrix"), TagSelectorTests::matrix);
        register.accept(key("fake_player_basics"), PlayerCommandTests::basics);
        register.accept(key("fake_player_tag_scenario"), PlayerCommandTests::tagScenario);
        register.accept(key("poi_villager_claims_bed"), PoiTests::claimsBed);
        register.accept(key("poi_villager_claims_bed_placed_later"), PoiTests::claimsBedPlacedLater);
        register.accept(key("poi_villager_claims_bed_freed_later"), PoiTests::claimsBedFreedLater);
        register.accept(key("poi_villager_takes_job"), PoiTests::takesJob);
        register.accept(key("poi_far_job_site"), PoiTests::farJobSite);
        register.accept(key("poi_locked_villager_takes_job_placed_later"), PoiTests::lockedVillagerTakesJobPlacedLater);
        register.accept(key("poi_one_workstation_two_villagers"), PoiTests::oneWorkstationTwoVillagers);
        register.accept(key("brain_villager_sleeps_at_night"), BrainTests::villagerSleepsAtNight);
        register.accept(key("brain_piglin_admires_gold"), BrainTests::piglinAdmiresGold);
        register.accept(key("brain_all_types"), MobAiTests::allBrainTypes);
        register.accept(key("path_zombie_hunts_through_maze"), PathfindingTests::zombieHuntsThroughMaze);
        register.accept(key("path_villager_walks_to_target"), PathfindingTests::villagerWalksToTarget);
        register.accept(key("inside_fire_ignites"), InsideBlockTests::fireIgnitesAndHurts);
        register.accept(key("inside_lava_ignites"), InsideBlockTests::lavaIgnitesAndHurts);
        register.accept(key("inside_lava_cauldron_ignites"), InsideBlockTests::lavaCauldronIgnites);
        register.accept(key("inside_water_extinguishes"), InsideBlockTests::waterExtinguishes);
        register.accept(key("inside_water_cauldron_drains"), InsideBlockTests::waterCauldronExtinguishesAndDrains);
        register.accept(key("inside_powder_snow_freezes"), InsideBlockTests::powderSnowFreezes);
        register.accept(key("inside_burning_melts_powder_snow"), InsideBlockTests::burningMobMeltsPowderSnow);
        register.accept(key("inside_mixed_field"), InsideBlockTests::mixedField);
        register.accept(key("tracker_pairs_and_unpairs"), TrackerTests::pairsAndUnpairs);
        register.accept(key("tracker_moving_entities_reach_player"), TrackerTests::movingEntitiesReachPlayer);
        register.accept(key("ally_zombie_targets_villager"), MobAiTests::zombieTargetsVillager);
        register.accept(key("ally_plugin_rule_respected"), MobAiTests::pluginAllyRuleRespected);
        register.accept(key("los_zombie_targets_visible_villager"), MobAiTests::zombieTargetsVisibleVillager);
        register.accept(key("sort_sensor_memory_nearest_first"), MobAiTests::sensorMemoryNearestFirst);
        register.accept(key("look_interact_villager_targets_player"), MobAiTests::villagerTargetsNearbyPlayer);
        register.accept(key("dropper_drops_into_up_column"), DropperTests::dropsIntoUpColumn);
        register.accept(key("dropper_drops_into_down_column"), DropperTests::dropsIntoDownColumn);
        register.accept(key("dropper_drops_into_water"), DropperTests::dropsIntoWater);
        register.accept(key("dropper_drops_down_into_up_column"), DropperTests::dropsDownIntoUpColumn);
        register.accept(key("dropper_drops_down_into_down_column"), DropperTests::dropsDownIntoDownColumn);
        register.accept(key("relight_direct_section_writes"), RelightTests::relightsDirectSectionWrites);
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

    // A redstone block powers a 3-long dust line into a lamp.
    private static void redstoneDustLightsLamp(final GameTestHelper helper) {
        for (int x = 2; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 1, 4), Blocks.REDSTONE_WIRE);
        }
        final BlockPos lamp = new BlockPos(5, 1, 4);
        helper.setBlock(lamp, Blocks.REDSTONE_LAMP);
        // setBlock places the wire's default state, which is a "dot" that powers nothing sideways.
        // Start from the cross state like a player placement does and let the neighbours shape it.
        final var cross = Blocks.REDSTONE_WIRE.defaultBlockState()
            .setValue(net.minecraft.world.level.block.RedstoneWireBlock.NORTH, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE)
            .setValue(net.minecraft.world.level.block.RedstoneWireBlock.SOUTH, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE)
            .setValue(net.minecraft.world.level.block.RedstoneWireBlock.EAST, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE)
            .setValue(net.minecraft.world.level.block.RedstoneWireBlock.WEST, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE);
        for (int x = 2; x <= 4; x++) {
            final BlockPos abs = helper.absolutePos(new BlockPos(x, 1, 4));
            helper.getLevel().setBlock(abs, net.minecraft.world.level.block.Block.updateFromNeighbourShapes(cross, helper.getLevel(), abs), 3);
        }
        helper.setBlock(new BlockPos(1, 1, 4), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(2, 0, 4));
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(1, 1, 4));
            helper.assertBlockPresent(Blocks.REDSTONE_WIRE, new BlockPos(2, 1, 4));
            helper.assertBlockProperty(new BlockPos(2, 1, 4), net.minecraft.world.level.block.RedstoneWireBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(4, 1, 4), net.minecraft.world.level.block.RedstoneWireBlock.EAST,
                net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE);
            helper.assertBlockProperty(new BlockPos(4, 1, 4), net.minecraft.world.level.block.RedstoneWireBlock.POWER, 13);
            helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true);
        });
    }

    // `execute if entity @e[..., nbt={...}]` must match exactly the entities whose saved NBT
    // contains the compound, and nothing else.
    private static void selectorNbtFiltersEntities(final GameTestHelper helper) {
        final Zombie tagged = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 1, 2));
        tagged.setNoAi(true);
        tagged.addTag("silk_nbt_a");
        tagged.setCustomName(net.minecraft.network.chat.Component.literal("silk_match"));
        final Zombie other = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(6, 1, 6));
        other.setNoAi(true);
        other.addTag("silk_nbt_a");

        final BlockPos hit = helper.absolutePos(new BlockPos(4, 1, 1));
        final BlockPos miss = helper.absolutePos(new BlockPos(4, 1, 7));
        final BlockPos count = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.runAfterDelay(2, () -> {
            run(helper, "scoreboard objectives add silk_test dummy");
            run(helper, "execute if entity @e[type=zombie,tag=silk_nbt_a,nbt={CustomName:\"silk_match\"}] run setblock "
                + xyz(hit) + " minecraft:gold_block");
            run(helper, "execute if entity @e[type=zombie,tag=silk_nbt_a,nbt={CustomName:\"nobody\"}] run setblock "
                + xyz(miss) + " minecraft:gold_block");
            // exactly one zombie matches the compound: store the count as a comparator-free block choice
            run(helper, "execute store result score #silk_nbt silk_test if entity @e[type=zombie,tag=silk_nbt_a,nbt={Tags:[\"silk_nbt_a\"]}]");
            run(helper, "execute if score #silk_nbt silk_test matches 2 run setblock " + xyz(count) + " minecraft:diamond_block");
        });
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(4, 1, 1));
            helper.assertBlockNotPresent(Blocks.GOLD_BLOCK, new BlockPos(4, 1, 7));
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(4, 1, 4));
        });
    }

    private static void run(final GameTestHelper helper, final String command) {
        final var server = helper.getLevel().getServer();
        final var source = server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, command);
    }

    private static String xyz(final BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
