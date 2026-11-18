package com.mohistmc.silkard.paper.core;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static counterparts of player and packet helpers that Paper adds to vanilla classes.
 */
public final class PaperPlayers {

    private static final Logger LOGGER = LoggerFactory.getLogger("Silkard");

    private PaperPlayers() {
    }

    /**
     * A section update packet for explicit block states. Vanilla only builds one from a live
     * chunk section, so the packet is decoded from its own wire format instead.
     */
    public static ClientboundSectionBlocksUpdatePacket sectionBlocksUpdate(SectionPos sectionPos, short[] positions, BlockState[] states) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            SectionPos.STREAM_CODEC.encode(buf, sectionPos);
            buf.writeVarInt(positions.length);
            for (int i = 0; i < positions.length; i++) {
                buf.writeVarLong((long) Block.getId(states[i]) << 12 | positions[i]);
            }
            return ClientboundSectionBlocksUpdatePacket.STREAM_CODEC.decode(buf);
        } finally {
            buf.release();
        }
    }

    /** A player info packet that only changes whether {@code uuid} is shown in the tab list. */
    public static ClientboundPlayerInfoUpdatePacket updateListed(UUID uuid, boolean listed) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), List.of());
        packet.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(uuid, null, listed, 0, GameType.DEFAULT_MODE, null, false, 0, null));
        return packet;
    }

    /**
     * Spawns the entity sitting on the player's left or right shoulder into the world and clears
     * that shoulder.
     *
     * @return the spawned entity, or {@code null} if the shoulder was empty or it failed to load
     */
    public static @Nullable Entity releaseShoulderEntity(ServerPlayer player, boolean left) {
        CompoundTag tag = left ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (tag.isEmpty()) {
            return null;
        }
        ServerLevel level = player.level();
        Entity spawned = null;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
            Entity entity = EntityType.create(
                TagValueInput.create(reporter.forChild(() -> ".shoulder"), level.registryAccess(), tag),
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false)
            ).orElse(null);
            if (entity != null) {
                if (entity instanceof TamableAnimal tamed) {
                    tamed.setOwner(player);
                }
                entity.setPos(player.getX(), player.getY() + 0.7F, player.getZ());
                if (level.addWithUUID(entity)) {
                    spawned = entity;
                }
            }
        }
        if (spawned != null) {
            if (left) {
                player.setShoulderEntityLeft(new CompoundTag());
            } else {
                player.setShoulderEntityRight(new CompoundTag());
            }
        }
        return spawned;
    }

    /** Keys of the chunks currently in the player's chunk tracking view. */
    public static LongSet getSentChunkKeys(ServerPlayer player) {
        LongSet keys = new LongOpenHashSet();
        player.getChunkTrackingView().forEach(pos -> keys.add(pos.pack()));
        return LongSets.unmodifiable(keys);
    }
}
