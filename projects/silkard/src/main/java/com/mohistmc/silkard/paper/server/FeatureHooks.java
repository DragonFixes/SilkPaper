package com.mohistmc.silkard.paper.server;

import io.papermc.paper.world.chunk.ChunkGenerationState;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Paper's {@code io.papermc.paper.FeatureHooks} for Paper's CraftBukkit server classes, written
 * against the vanilla chunk system. Paper backs these with Moonrise; Silkard leaves vanilla's
 * chunk system in place so chunk mods (C2ME and others) keep working, and uses only its public
 * entry points here. Method names match Paper's so call sites read the same.
 */
public final class FeatureHooks {

    private FeatureHooks() {
    }

    public static LevelChunkSection createSection(final PalettedContainerFactory palettedContainerFactory, final @Nullable Level level, final @Nullable ChunkPos chunkPos, final int chunkSection) {
        // TODO Silkard - Anti-Xray is not ported, so the section takes no level or position
        return new LevelChunkSection(palettedContainerFactory);
    }

    public static void sendChunkRefreshPackets(final List<ServerPlayer> playersInRange, final LevelChunk chunk) {
        final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, chunk.getLevel().getLightEngine(), null, null);
        for (final ServerPlayer player : playersInRange) {
            if (player.connection == null) {
                continue;
            }
            player.connection.send(packet);
        }
    }

    public static PalettedContainer<BlockState> emptyPalettedBlockContainer() {
        return new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
    }

    public static void dumpAllChunkLoadInfo(final MinecraftServer server, final boolean isLongTimeout) {
        // TODO Silkard - the vanilla chunk system keeps no per-chunk load diagnostics to dump
    }

    /**
     * Loads the chunk's entities if needed, waiting on the entity deserializer on this thread,
     * the same way CraftBukkit does without Paper's chunk system.
     */
    public static Entity[] getChunkEntities(final ServerLevel level, final int chunkX, final int chunkZ) {
        if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
            level.getChunk(chunkX, chunkZ); // transient load for this tick
        }

        final PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityManager = level.entityManager;
        final long pair = ChunkPos.pack(chunkX, chunkZ);

        if (!entityManager.areEntitiesLoaded(pair)) {
            entityManager.ensureChunkQueuedForLoad(pair);

            final ConsecutiveExecutor mailbox = ((EntityStorage) entityManager.permanentStorage).entityDeserializerQueue;
            final BooleanSupplier loaded = () -> {
                if (entityManager.areEntitiesLoaded(pair)) {
                    return true;
                }
                if (!entityManager.isPending(pair)) {
                    entityManager.ensureChunkQueuedForLoad(pair);
                }
                entityManager.tick();
                return entityManager.areEntitiesLoaded(pair);
            };

            // entity NBT is turned into entities on the main thread, so drain the queue here
            while (!loaded.getAsBoolean()) {
                if (mailbox.size() != 0) {
                    mailbox.run();
                } else {
                    Thread.yield();
                    LockSupport.parkNanos("waiting for entity loading", 100000L);
                }
            }
        }

        return entityManager.getEntities(new ChunkPos(chunkX, chunkZ)).stream()
            .map(net.minecraft.world.entity.Entity::getBukkitEntity)
            .filter(Objects::nonNull)
            .toArray(Entity[]::new);
    }

    public static int getViewDistance(final ServerLevel level) {
        return level.getChunkSource().chunkMap.serverViewDistance;
    }

    public static int getSimulationDistance(final ServerLevel level) {
        return level.getChunkSource().chunkMap.getDistanceManager().simulationDistance;
    }

    public static int getSendViewDistance(final ServerLevel level) {
        // vanilla sends every chunk it loads for a player, so the send distance is the view distance
        return getViewDistance(level);
    }

    public static void setViewDistance(final ServerLevel level, final int distance) {
        if (distance < 2 || distance > 32) {
            throw new IllegalArgumentException("View distance " + distance + " is out of range of [2, 32]");
        }
        level.getChunkSource().setViewDistance(distance);
    }

    public static void setSimulationDistance(final ServerLevel level, final int distance) {
        if (distance < 2 || distance > 32) {
            throw new IllegalArgumentException("Simulation distance " + distance + " is out of range of [2, 32]");
        }
        level.getChunkSource().setSimulationDistance(distance);
    }

    public static void setSendViewDistance(final ServerLevel level, final int distance) {
        // TODO Silkard - vanilla has no separate send distance; the request is ignored
    }

    public static Executor getWorldgenExecutor() {
        return Runnable::run;
    }

    public static ChunkGenerationState getChunkGenerationState(final ServerLevel level, final int chunkX, final int chunkZ) {
        return getChunkGenerationStateAsync(level, chunkX, chunkZ).join();
    }

    public static CompletableFuture<ChunkGenerationState> getChunkGenerationStateAsync(final ServerLevel level, final int chunkX, final int chunkZ) {
        final ChunkGenerationState loaded = getLoadedChunkGenerationState(level, chunkX, chunkZ);
        if (loaded != null) {
            return CompletableFuture.completedFuture(loaded);
        }
        return level.getChunkSource().chunkMap.readChunk(new ChunkPos(chunkX, chunkZ))
            .thenApply((Optional<CompoundTag> tag) -> classifyChunkGenerationState(tag.orElse(null), chunkX, chunkZ));
    }

    private static @Nullable ChunkGenerationState getLoadedChunkGenerationState(final ServerLevel level, final int chunkX, final int chunkZ) {
        final ChunkHolder holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(ChunkPos.pack(chunkX, chunkZ));
        if (holder == null) {
            return null;
        }
        final ChunkAccess chunk = holder.getLatestChunk();
        if (chunk == null) {
            return null;
        }
        return chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL) ? ChunkGenerationState.GENERATED : ChunkGenerationState.PARTIAL;
    }

    private static ChunkGenerationState classifyChunkGenerationState(final @Nullable CompoundTag data, final int chunkX, final int chunkZ) {
        if (data == null) {
            return ChunkGenerationState.NOT_GENERATED;
        }

        final int dataVersion = NbtUtils.getDataVersion(data, -1);
        final int currentVersion = SharedConstants.getCurrentVersion().dataVersion().version();
        if (dataVersion > currentVersion) {
            throw new IllegalStateException("Chunk (" + chunkX + ", " + chunkZ + ") was saved by a newer version (data version " + dataVersion + ", current " + currentVersion + ")");
        }
        if (dataVersion < currentVersion) {
            // status may not be accurate before the data is upgraded, and loading rewrites the chunk regardless
            return ChunkGenerationState.OUTDATED;
        }

        if (data.getIntOr(SerializableChunkData.X_POS_TAG, 0) != chunkX || data.getIntOr(SerializableChunkData.Z_POS_TAG, 0) != chunkZ) {
            // loading relocates the chunk
            return ChunkGenerationState.OUTDATED;
        }

        final ChunkStatus status = data.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
        final BelowZeroRetrogen retrogen = data.read("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
        if (retrogen == null) {
            return status.isOrAfter(ChunkStatus.FULL) ? ChunkGenerationState.GENERATED : ChunkGenerationState.PARTIAL;
        }
        // upgraded pre-1.18 chunks are stored with an empty status and their original status as the retrogen target
        if (status.isOrAfter(ChunkStatus.FULL) || retrogen.targetStatus().isOrAfter(ChunkStatus.SPAWN)) {
            return ChunkGenerationState.OUTDATED;
        }
        return ChunkGenerationState.PARTIAL;
    }
}
