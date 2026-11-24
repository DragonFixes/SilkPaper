package com.mohistmc.silkard.gametest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import org.slf4j.LoggerFactory;

/**
 * Entity tracking: which players see which entities, checked through fake players that receive real
 * packets. Every entity in range is paired with every player in range (spawn packet sent), unpaired when
 * the player leaves (remove packet sent) and re-paired when the player comes back, and every moving
 * entity's movement reaches the player.
 */
final class TrackerTests {
    private TrackerTests() {
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("SilkGameTests");

    private static TestPlayer spectator(final GameTestHelper helper, final String name) {
        final TestPlayer fake = TestPlayer.join(helper, name, new BlockPos(4, 4, 4), false);
        fake.player().setGameMode(GameType.SPECTATOR);
        return fake;
    }

    private static List<Mob> cows(final GameTestHelper helper, final int count) {
        final List<Mob> mobs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Mob cow = helper.spawn(EntityTypes.COW, new BlockPos(1 + i % 7, 1, 1 + (i / 7) % 7));
            cow.setNoAi(true);
            cow.setPersistenceRequired();
            mobs.add(cow);
        }
        return mobs;
    }

    private static boolean tracks(final Mob mob, final ServerPlayer player) {
        final ChunkMap.TrackedEntity tracker = ((ServerLevel) mob.level()).getChunkSource().chunkMap.entityMap.get(mob.getId());
        return tracker != null && tracker.seenBy.contains(player.connection);
    }


    private static int entityId(final Packet<?> packet) {
        if (packet instanceof ClientboundAddEntityPacket add) {
            return add.getId();
        }
        if (packet instanceof ClientboundEntityPositionSyncPacket sync) {
            return sync.id();
        }
        if (packet instanceof ClientboundTeleportEntityPacket teleport) {
            return teleport.id();
        }
        if (packet instanceof ClientboundMoveEntityPacket) {
            try {
                final var field = ClientboundMoveEntityPacket.class.getDeclaredField("entityId");
                field.setAccessible(true);
                return field.getInt(packet);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return -1;
    }

    private static Set<Integer> idsOf(final List<Packet<?>> packets, final Class<?> type) {
        final Set<Integer> ids = new HashSet<>();
        for (final Packet<?> packet : packets) {
            if (type.isInstance(packet)) {
                if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
                    remove.entityIds().forEach(ids::add);
                } else {
                    ids.add(entityId(packet));
                }
            }
        }
        return ids;
    }

    private static Set<Integer> movedIds(final List<Packet<?>> packets) {
        final Set<Integer> ids = new HashSet<>();
        for (final Packet<?> packet : packets) {
            if (packet instanceof ClientboundMoveEntityPacket || packet instanceof ClientboundEntityPositionSyncPacket || packet instanceof ClientboundTeleportEntityPacket) {
                ids.add(entityId(packet));
            }
        }
        return ids;
    }

    private static String missing(final List<Mob> mobs, final java.util.function.Predicate<Mob> ok, final String what) {
        final List<Integer> bad = new ArrayList<>();
        for (final Mob mob : mobs) {
            if (!ok.test(mob)) {
                bad.add(mob.getId());
            }
        }
        return bad.isEmpty() ? "" : "\n  " + bad.size() + "/" + mobs.size() + " " + what + " (ids " + bad.subList(0, Math.min(8, bad.size())) + ")";
    }

    /**
     * 80 cows, players A and B: both pair with every cow (spawn packets); B goes 3000 blocks away and is
     * unpaired from all (remove packets) while A keeps them; B comes back and is paired again. Packets can
     * wait in a connection's queue behind chunk packets that are not ready yet, so each stage collects
     * packets over ticks and moves on once everything it expects has arrived (or the test times out).
     */
    static void pairsAndUnpairs(final GameTestHelper helper) {
        final TestPlayer a = spectator(helper, "TrkA");
        final TestPlayer b = spectator(helper, "TrkB");
        final List<Mob> mobs = cows(helper, 80);
        final Set<Integer> addedA = new HashSet<>();
        final Set<Integer> addedB = new HashSet<>();
        final Set<Integer> removedB = new HashSet<>();
        final Set<Integer> readdedB = new HashSet<>();
        final int[] stage = {0};
        final String[] last = {""};
        // diagnostics: what B received per cow (stage:tick:packet), to explain a missing packet
        final java.util.Map<Integer, List<String>> seenByB = new java.util.HashMap<>();
        helper.onEachTick(() -> {
            if (stage[0] == 3) {
                return;
            }
            addedA.addAll(idsOf(a.drainPackets(), ClientboundAddEntityPacket.class));
            final List<Packet<?>> packetsB = b.drainPackets();
            for (final Packet<?> packet : packetsB) {
                final List<Integer> ids = new ArrayList<>();
                if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
                    remove.entityIds().forEach(ids::add);
                } else {
                    ids.add(entityId(packet));
                }
                for (final int id : ids) {
                    if (id >= 0) {
                        final List<String> l = seenByB.computeIfAbsent(id, k -> new ArrayList<>());
                        if (l.size() < 10) {
                            l.add(stage[0] + ":" + helper.getTick() + ":" + packet.getClass().getSimpleName().replace("Clientbound", "").replace("Packet", ""));
                        }
                    }
                }
            }
            switch (stage[0]) {
                case 0 -> {
                    addedB.addAll(idsOf(packetsB, ClientboundAddEntityPacket.class));
                    last[0] = missing(mobs, m -> tracks(m, a.player()), "cows not tracked by A")
                        + missing(mobs, m -> tracks(m, b.player()), "cows not tracked by B")
                        + missing(mobs, m -> addedA.contains(m.getId()), "cows without a spawn packet to A")
                        + missing(mobs, m -> addedB.contains(m.getId()), "cows without a spawn packet to B");
                    if (last[0].isEmpty()) {
                        stage[0] = 1;
                        LOG.info("[SilkGameTests] tracker pairs/unpairs: stage 0 done on tick {}", helper.getTick());
                        b.teleport(b.player().getX() + 3000, b.player().getY(), b.player().getZ());
                    }
                }
                case 1 -> {
                    removedB.addAll(idsOf(packetsB, ClientboundRemoveEntitiesPacket.class));
                    last[0] = missing(mobs, m -> !tracks(m, b.player()), "cows still tracked by B after it left")
                        + missing(mobs, m -> removedB.contains(m.getId()), "cows without a remove packet to B")
                        + missing(mobs, m -> tracks(m, a.player()), "cows A lost while B was away");
                    if (last[0].isEmpty()) {
                        stage[0] = 2;
                        LOG.info("[SilkGameTests] tracker pairs/unpairs: stage 1 done on tick {}", helper.getTick());
                        b.teleport(b.player().getX() - 3000, b.player().getY(), b.player().getZ());
                    }
                }
                case 2 -> {
                    readdedB.addAll(idsOf(packetsB, ClientboundAddEntityPacket.class));
                    last[0] = missing(mobs, m -> tracks(m, b.player()), "cows not tracked by B after it came back")
                        + missing(mobs, m -> readdedB.contains(m.getId()), "cows without a new spawn packet to B");
                    if (!last[0].isEmpty()) {
                        final Mob first = mobs.stream().filter(m -> !readdedB.contains(m.getId())).findFirst().orElse(null);
                        if (first != null) {
                            last[0] += "\n  first such cow " + first.getId() + " at " + first.blockPosition().toShortString() + ", B at " + b.player().blockPosition().toShortString()
                                + ", B received for it: " + seenByB.getOrDefault(first.getId(), List.of());
                        }
                    }
                    if (last[0].isEmpty()) {
                        stage[0] = 3;
                        LOG.info("[SilkGameTests] tracker pairs/unpairs: stage 2 done on tick {}", helper.getTick());
                    }
                }
                default -> {
                }
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(stage[0] == 3, Component.literal("stage " + stage[0] + ":" + last[0]));
            TestPlayer.leaveAll(helper);
        });
    }

    /** 80 cows moved a little every tick for 20 ticks: every one of them sends movement to the watching player. */
    static void movingEntitiesReachPlayer(final GameTestHelper helper) {
        final TestPlayer a = spectator(helper, "TrkM");
        final List<Mob> mobs = cows(helper, 80);
        helper.runAfterDelay(3, a::drainPackets);
        for (int t = 4; t < 24; t++) {
            final double dx = (t % 2 == 0) ? 0.3 : -0.3;
            helper.runAfterDelay(t, () -> mobs.forEach(m -> m.setPos(m.getX() + dx, m.getY(), m.getZ())));
        }
        helper.runAfterDelay(26, () -> {
            final Set<Integer> moved = movedIds(a.drainPackets());
            final String p = missing(mobs, m -> moved.contains(m.getId()), "moving cows whose movement never reached the player");
            TestPlayer.leaveAll(helper);
            if (!p.isEmpty()) {
                helper.fail(Component.literal(p));
            } else {
                helper.succeed();
            }
        });
    }

}
