package com.mohistmc.silkard.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.LoggerFactory;

/**
 * Mob AI behaviour: target selection with line of sight, the Paper Allies API, brain sensor order,
 * look-and-interact, and every mob type with a brain ticking without errors. A spectating fake player
 * stands by in each test, so entity activation range never freezes the mobs.
 */
final class MobAiTests {
    private MobAiTests() {
    }

    private static TestPlayer watcher(final GameTestHelper helper, final String name) {
        final TestPlayer watcher = TestPlayer.join(helper, name, new BlockPos(4, 4, 4), false);
        watcher.player().setGameMode(GameType.SPECTATOR);
        return watcher;
    }

    private static <T extends Mob> T still(final GameTestHelper helper, final EntityType<T> type, final BlockPos pos) {
        final T mob = helper.spawn(type, pos);
        mob.setPersistenceRequired();
        mob.setNoAi(true);
        return mob;
    }

    private static String name(final Entity e) {
        return e == null ? "none" : e.getType().toShortString() + "#" + e.getId();
    }

    private static String order(final List<? extends Entity> list) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < Math.min(8, list.size()); i++) {
            b.append(i == 0 ? "" : ",").append(list.get(i).getId());
        }
        return b.append(list.size() > 8 ? ",..." : "").toString();
    }

    /** A zombie ignores the nearer villager behind glass and targets the farther one in sight. */
    static void zombieTargetsVisibleVillager(final GameTestHelper helper) {
        watcher(helper, "LosW");
        for (int y = 1; y <= 3; y++) {
            for (int z = 0; z <= 8; z++) {
                helper.setBlock(new BlockPos(2, y, z), Blocks.GLASS);
            }
        }
        final Mob zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 1, 4));
        zombie.setPersistenceRequired();
        final Villager hidden = still(helper, EntityTypes.VILLAGER, new BlockPos(3, 1, 4));
        // visible: in the open column at the end of the wall line
        for (int y = 1; y <= 3; y++) {
            helper.setBlock(new BlockPos(2, y, 0), Blocks.AIR);
        }
        final Villager visible = still(helper, EntityTypes.VILLAGER, new BlockPos(1, 1, 0));
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getTarget() == visible, Component.literal("zombie target: " + name(zombie.getTarget())
                + ", expected the visible villager (hidden one is " + name(hidden) + ")"));
            TestPlayer.leaveAll(helper);
        });
    }

    /** A zombie with a villager in sight targets it. */
    static void zombieTargetsVillager(final GameTestHelper helper) {
        watcher(helper, "AllyZ");
        final Mob zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 1, 4));
        zombie.setPersistenceRequired();
        final Villager villager = still(helper, EntityTypes.VILLAGER, new BlockPos(7, 1, 4));
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getTarget() == villager, Component.literal("zombie target: " + name(zombie.getTarget())));
            TestPlayer.leaveAll(helper);
        });
    }

    /**
     * A plugin rule (Paper Allies API) makes a zombie treat villagers as allies: isAlliedTo says so and the
     * zombie does not target the villager; removing the rule ends it.
     */
    static void pluginAllyRuleRespected(final GameTestHelper helper) {
        watcher(helper, "AllyW");
        final Mob zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 1, 4));
        zombie.setPersistenceRequired();
        final Villager villager = still(helper, EntityTypes.VILLAGER, new BlockPos(7, 1, 4));
        final Key key = Key.key("silkpaper", "ally_test");
        zombie.getBukkitEntity().addAdditionalAlliedRule(key, other -> other instanceof org.bukkit.entity.Villager);
        helper.runAfterDelay(60, () -> {
            final StringBuilder f = new StringBuilder();
            if (!zombie.isAlliedTo(villager)) {
                f.append("\n  the plugin rule is ignored: zombie is not allied to the villager");
            }
            if (zombie.getTarget() == villager) {
                f.append("\n  the zombie targets an allied villager");
            }
            zombie.getBukkitEntity().removeAdditionalAlliedRule(key);
            if (zombie.isAlliedTo(villager)) {
                f.append("\n  rule removed, but zombie is still allied to the villager");
            }
            TestPlayer.leaveAll(helper);
            if (!f.isEmpty()) {
                helper.fail(Component.literal(f.toString()));
            } else {
                helper.succeed();
            }
        });
    }

    /** A villager's NEAREST_LIVING_ENTITIES memory lists its neighbours nearest first. */
    static void sensorMemoryNearestFirst(final GameTestHelper helper) {
        watcher(helper, "SortW");
        final Villager body = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 1, 1));
        body.setPersistenceRequired();
        for (int i = 0; i < 6; i++) {
            still(helper, EntityTypes.VILLAGER, new BlockPos(7 - i, 1, 7 - i % 2));
        }
        helper.succeedWhen(() -> {
            final var memory = body.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
            helper.assertTrue(memory.isPresent() && memory.get().size() >= 6, Component.literal("sensor memory: " + memory));
            final List<LivingEntity> list = memory.get();
            for (int i = 1; i < list.size(); i++) {
                helper.assertTrue(body.distanceToSqr(list.get(i - 1)) <= body.distanceToSqr(list.get(i)), Component.literal("not nearest first: " + order(list)));
            }
            TestPlayer.leaveAll(helper);
        });
    }

    /**
     * The look-and-interact behaviour, run on a villager whose sensor saw a crowd and a player 1.5 blocks
     * away: it starts and picks the player.
     */
    static void villagerTargetsNearbyPlayer(final GameTestHelper helper) {
        final TestPlayer player = TestPlayer.join(helper, "LookC", new BlockPos(4, 1, 6), false);
        player.player().setGameMode(GameType.SURVIVAL);
        final Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 1, 4));
        villager.setPersistenceRequired();
        // a crowd sorted in front of the player in the sensor's list
        for (int i = 0; i < 12; i++) {
            still(helper, EntityTypes.VILLAGER, new BlockPos(1 + i % 3, 1, 1 + i / 3 % 3));
        }
        final var behavior = SetLookAndInteract.create(EntityTypes.PLAYER, 4);
        helper.succeedWhen(() -> {
            player.teleport(villager.getX() + 1.5, villager.getY(), villager.getZ());
            final Brain<Villager> brain = villager.getBrain();
            final var visible = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            helper.assertTrue(visible.isPresent() && visible.get().nearbyEntities().contains(player.player()),
                Component.literal("sensor has not yet seen the player: " + visible.map(NearestVisibleLivingEntities::nearbyEntities)));
            brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            final boolean started;
            try {
                started = behavior.tryStart(helper.getLevel(), villager, helper.getLevel().getGameTime());
                helper.assertTrue(started && brain.isMemoryValue(MemoryModuleType.INTERACTION_TARGET, player.player()),
                    Component.literal("look-and-interact did not pick the player: started " + started + ", target " + brain.getMemory(MemoryModuleType.INTERACTION_TARGET)));
                behavior.doStop(helper.getLevel(), villager, helper.getLevel().getGameTime());
            } finally {
                brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            }
            TestPlayer.leaveAll(helper);
        });
    }

    /** Types left out: bosses and mobs that would wreck the test area. */
    private static final Set<EntityType<?>> SKIP = Set.of(EntityTypes.ENDER_DRAGON, EntityTypes.WITHER, EntityTypes.WARDEN, EntityTypes.PLAYER);

    /** Every mob type that has a brain ticks side by side for 300 ticks; nothing may throw. */
    static void allBrainTypes(final GameTestHelper helper) {
        watcher(helper, "BrainW");
        final List<String> types = new ArrayList<>();
        int x = 0;
        for (final EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (SKIP.contains(type)) {
                continue;
            }
            final Entity probe;
            try {
                probe = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            } catch (final Throwable t) {
                continue;
            }
            if (!(probe instanceof Mob mob) || probe.isRemoved()) {
                continue;
            }
            if (!(mob.getBrain().isActive(Activity.CORE) || !mob.getBrain().getActiveActivities().isEmpty())) {
                continue; // goal-selector mobs have an empty brain
            }
            final BlockPos at = helper.absolutePos(new BlockPos(1 + (x % 7), 1, 1 + (x / 7) % 7));
            x++;
            mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            mob.setPersistenceRequired();
            if (mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin piglin) {
                piglin.setImmuneToZombification(true);
            }
            if (mob instanceof net.minecraft.world.entity.monster.hoglin.Hoglin hoglin) {
                hoglin.setImmuneToZombification(true);
            }
            helper.getLevel().addFreshEntity(mob);
            types.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
        }
        helper.runAfterDelay(300, () -> {
            LoggerFactory.getLogger("SilkGameTests").info("[SilkGameTests] brain types ticked 300 ticks: {} {}", types.size(), types);
            TestPlayer.leaveAll(helper);
            helper.succeed();
        });
    }
}
