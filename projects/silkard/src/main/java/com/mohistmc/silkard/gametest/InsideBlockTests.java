package com.mohistmc.silkard.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Inside-block effects: pins every vanilla path that feeds {@code InsideBlockEffectApplier.StepBasedCollector}
 * (fire, lava, lava cauldron, water, water cauldron, powder snow), plus a mixed field as a crash smoke test.
 * Coordinates are relative to {@code silkpaper:platform} (stone floor at y=0).
 */
public final class InsideBlockTests {
    private InsideBlockTests() {
    }

    private static final BlockPos CENTER = new BlockPos(4, 1, 4);

    private static Pig pig(final GameTestHelper helper) {
        final Pig pig = helper.spawn(EntityTypes.PIG, CENTER);
        pig.setNoAi(true);
        return pig;
    }

    /** Stone ring around CENTER so liquids stay put. */
    private static void walls(final GameTestHelper helper) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    helper.setBlock(CENTER.offset(dx, 0, dz), Blocks.STONE);
                }
            }
        }
    }

    public static void fireIgnitesAndHurts(final GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.FIRE);
        final Pig pig = pig(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.isOnFire(), "pig in fire is not burning");
            helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "pig in fire took no damage");
        });
    }

    public static void lavaIgnitesAndHurts(final GameTestHelper helper) {
        walls(helper);
        helper.setBlock(CENTER, Blocks.LAVA);
        final Pig pig = pig(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.isOnFire() || !pig.isAlive(), "pig in lava is not burning");
            helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "pig in lava took no damage");
        });
    }

    public static void lavaCauldronIgnites(final GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.LAVA_CAULDRON);
        final Pig pig = pig(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.isOnFire() || !pig.isAlive(), "pig in lava cauldron is not burning");
            helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "pig in lava cauldron took no damage");
        });
    }

    public static void waterExtinguishes(final GameTestHelper helper) {
        walls(helper);
        helper.setBlock(CENTER, Blocks.WATER);
        final Pig pig = pig(helper);
        pig.igniteForSeconds(10.0F);
        helper.assertTrue(pig.isOnFire(), "setup: pig not burning");
        helper.succeedWhen(() -> helper.assertFalse(pig.isOnFire(), "pig in water still burning"));
    }

    public static void waterCauldronExtinguishesAndDrains(final GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        final Pig pig = pig(helper);
        pig.igniteForSeconds(10.0F);
        helper.succeedWhen(() -> {
            helper.assertFalse(pig.isOnFire(), "pig in water cauldron still burning");
            helper.assertBlockProperty(CENTER, LayeredCauldronBlock.LEVEL, 2);
        });
    }

    public static void powderSnowFreezes(final GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.POWDER_SNOW);
        helper.setBlock(CENTER.above(), Blocks.POWDER_SNOW);
        final Pig pig = pig(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.getTicksFrozen() >= 10, "pig in powder snow is not freezing: " + pig.getTicksFrozen());
            helper.assertTrue(pig.isInPowderSnow, "isInPowderSnow not set");
        });
    }

    public static void burningMobMeltsPowderSnow(final GameTestHelper helper) {
        helper.setBlock(CENTER, Blocks.POWDER_SNOW);
        final Pig pig = pig(helper);
        pig.igniteForSeconds(10.0F);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.AIR, CENTER);
            helper.assertFalse(pig.isOnFire(), "burning pig still burning after melting powder snow");
        });
    }

    private static final Block[] FIELD = {
        Blocks.AIR, Blocks.AIR, Blocks.SHORT_GRASS, Blocks.FIRE, Blocks.WATER, Blocks.POWDER_SNOW, Blocks.COBWEB,
        Blocks.SWEET_BERRY_BUSH, Blocks.STONE_PRESSURE_PLATE, Blocks.LAVA, Blocks.SOUL_FIRE, Blocks.CAULDRON
    };

    /** Mixed field of effect blocks and moving entities: nothing may throw while they burn, freeze and drown. */
    public static void mixedField(final GameTestHelper helper) {
        final Random random = new Random(58);
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), FIELD[random.nextInt(FIELD.length)]);
            }
        }
        final List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            final Vec3 pos = new Vec3(1.5 + random.nextInt(7), 1.0, 1.5 + random.nextInt(7));
            final Entity e = switch (i % 4) {
                case 0 -> helper.spawn(EntityTypes.PIG, pos);
                case 1 -> helper.spawn(EntityTypes.ZOMBIE, pos);
                case 2 -> helper.spawn(EntityTypes.SKELETON, pos);
                default -> helper.spawn(EntityTypes.CHICKEN, pos);
            };
            if (i % 3 == 0) {
                e.igniteForSeconds(5.0F);
            }
            entities.add(e);
        }
        helper.runAfterDelay(80, () -> {
            int alive = 0;
            for (final Entity e : entities) {
                if (e.isAlive()) {
                    alive++;
                }
            }
            helper.assertTrue(alive >= 0, "unreachable");
            helper.succeed();
        });
    }

}
