package com.mohistmc.silkard.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

/**
 * End-to-end pathfinding: mobs reach targets behind walls through goal-based navigation and through
 * brain walking. Runs in the 24x24 {@code silkpaper:arena} structure.
 */
final class PathfindingTests {
    private PathfindingTests() {
    }

    private static <T extends Mob> T mob(final GameTestHelper helper, final EntityType<T> type, final BlockPos pos, final boolean ai) {
        final T mob = helper.spawn(type, pos);
        mob.setPersistenceRequired();
        mob.setNoAi(!ai);
        mob.setOnGround(true); // a mob without AI never lands on its own, and navigation needs it on the ground
        return mob;
    }

    private static String describe(final @Nullable Path path) {
        if (path == null) {
            return "none";
        }
        return path.getNodeCount() + " nodes to " + path.getTarget().toShortString() + (path.canReach() ? " (reaches)" : " (does not reach)")
            + ", ends " + path.getEndNode();
    }

    /** A zombie hunts a villager across the arena through a gap in one glass wall and around a second one, and hits it. */
    static void zombieHuntsThroughMaze(final GameTestHelper helper) {
        final TestPlayer watcher = TestPlayer.join(helper, "PathW", new BlockPos(12, 5, 12), false);
        watcher.player().setGameMode(GameType.SPECTATOR);
        // a wall across the arena with a gap in the middle, and a second wall behind the gap to walk around
        for (int x = 1; x <= 22; x++) {
            for (int y = 1; y <= 3; y++) {
                if (x < 11 || x > 12) {
                    helper.setBlock(new BlockPos(x, y, 11), Blocks.GLASS);
                }
                if (x >= 6 && x <= 17) {
                    helper.setBlock(new BlockPos(x, y, 15), Blocks.GLASS);
                }
            }
        }
        final Zombie zombie = mob(helper, EntityTypes.ZOMBIE, new BlockPos(2, 1, 2), true);
        final Villager villager = mob(helper, EntityTypes.VILLAGER, new BlockPos(3, 1, 21), false);
        villager.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 255, false, false));
        helper.runAfterDelay(1, () -> zombie.setTarget(villager, org.bukkit.event.entity.EntityTargetEvent.TargetReason.CUSTOM, true));
        helper.succeedWhen(() -> {
            helper.assertTrue(villager.getLastHurtByMob() == zombie, Component.literal("the zombie has not hit the villager (zombie at "
                + helper.relativePos(zombie.blockPosition()).toShortString() + ", path " + describe(zombie.getNavigation().getPath()) + ")"));
            TestPlayer.leaveAll(helper);
        });
    }

    /**
     * Brain walking (MoveToTargetSink, which looks at the path's "can reach" straight away): a villager given a
     * walk target behind a wall walks around it and arrives; a villager boxed in glass, given a walk target
     * outside, records that it cannot reach it.
     */
    static void villagerWalksToTarget(final GameTestHelper helper) {
        final TestPlayer watcher = TestPlayer.join(helper, "PathV", new BlockPos(12, 5, 12), false);
        watcher.player().setGameMode(GameType.SPECTATOR);
        for (int x = 1; x <= 16; x++) {
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(new BlockPos(x, y, 11), Blocks.GLASS);
            }
        }
        for (int x = 18; x <= 22; x++) {
            for (int z = 2; z <= 6; z++) {
                for (int y = 1; y <= 3; y++) {
                    if (x == 18 || x == 22 || z == 2 || z == 6 || y == 3) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.GLASS);
                    }
                }
            }
        }
        final Villager walker = mob(helper, EntityTypes.VILLAGER, new BlockPos(3, 1, 3), true);
        final Villager boxed = mob(helper, EntityTypes.VILLAGER, new BlockPos(20, 1, 4), true);
        final BlockPos goal = helper.absolutePos(new BlockPos(3, 1, 19));
        final BlockPos outside = helper.absolutePos(new BlockPos(12, 1, 4));
        helper.onEachTick(() -> {
            if (!walker.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                walker.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(goal, 0.6F, 1));
            }
            if (!boxed.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                boxed.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(outside, 0.6F, 1));
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(walker.blockPosition().distManhattan(goal) <= 2, Component.literal("the villager has not arrived (at "
                + helper.relativePos(walker.blockPosition()).toShortString() + ", path " + describe(walker.getNavigation().getPath()) + ")"));
            helper.assertTrue(boxed.getBrain().hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE),
                Component.literal("the boxed villager does not know it cannot reach its walk target"));
            TestPlayer.leaveAll(helper);
        });
    }
}
