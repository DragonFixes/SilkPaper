package com.mohistmc.silkard.paper.server;

import ca.spottedleaf.concurrentutil.util.Priority;
import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

/**
 * Moonrise's platform hooks on top of vanilla's chunk system, found by
 * {@code PlatformHooks.get()} through {@code META-INF/services}. Paper's own implementation
 * ({@code ca.spottedleaf.moonrise.paper.PaperHooks}) drives Moonrise's chunk task scheduler,
 * which SilkPaper does not have: chunk loads go through {@link ServerChunkCache}, holder lists
 * come from {@link ChunkMap}, and the chunk system callbacks have nothing to notify.
 */
public final class SilkardPlatformHooks implements PlatformHooks {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Prefix of Moonrise's thread names and system properties ({@code -DSilkPaper.WorkerThreadCount},
     * {@code -DSilkPaper.MaxViewDistance}, {@code -DSilkPaper.NumaScheduling}).
     */
    @Override
    public String getBrand() {
        return "SilkPaper";
    }

    @Override
    public int getLightEmission(final BlockState blockState, final BlockGetter world, final BlockPos pos) {
        return blockState.getLightEmission();
    }

    @Override
    public Predicate<BlockState> maybeHasLightEmission() {
        return state -> state.getLightEmission() != 0;
    }

    @Override
    public boolean hasCurrentlyLoadingChunk() {
        return false;
    }

    @Override
    public LevelChunk getCurrentlyLoadingChunk(final GenerationChunkHolder holder) {
        return null;
    }

    @Override
    public void setCurrentlyLoading(final GenerationChunkHolder holder, final LevelChunk levelChunk) {
    }

    @Override
    public void chunkFullStatusComplete(final LevelChunk newChunk, final ProtoChunk original) {
    }

    @Override
    public boolean allowAsyncTicketUpdates() {
        // vanilla's ticket storage is only safe to change on the main thread
        return false;
    }

    @Override
    public void onChunkHolderTicketChange(final ServerLevel world, final ChunkHolder holder, final int oldLevel, final int newLevel) {
    }

    @Override
    public void chunkUnloadFromWorld(final LevelChunk chunk) {
    }

    @Override
    public void onFullChunkStatusChange(final LevelChunk chunk, final FullChunkStatus from, final FullChunkStatus to) {
    }

    @Override
    public void chunkSyncSave(final ServerLevel world, final ChunkAccess chunk, final SerializableChunkData data) {
    }

    @Override
    public void onChunkWatch(final ServerLevel world, final LevelChunk chunk, final ServerPlayer player) {
    }

    @Override
    public void onChunkUnWatch(final ServerLevel world, final ChunkPos chunk, final ServerPlayer player) {
    }

    @Override
    public void addToGetEntities(final Level world, final Entity entity, final AABB boundingBox, final Predicate<? super Entity> predicate, final List<Entity> into) {
        final Collection<EnderDragonPart> parts = world.dragonParts();
        if (parts.isEmpty()) {
            return;
        }
        for (final EnderDragonPart part : parts) {
            if (part != entity && part.getBoundingBox().intersects(boundingBox) && (predicate == null || predicate.test(part))) {
                into.add(part);
            }
        }
    }

    @Override
    public <T extends Entity> void addToGetEntities(final Level world, final EntityTypeTest<Entity, T> entityTypeTest, final AABB boundingBox,
                                                    final Predicate<? super T> predicate, final List<? super T> into, final int maxCount) {
        if (into.size() >= maxCount) {
            return;
        }
        final Collection<EnderDragonPart> parts = world.dragonParts();
        if (parts.isEmpty()) {
            return;
        }
        for (final EnderDragonPart part : parts) {
            if (!part.getBoundingBox().intersects(boundingBox)) {
                continue;
            }
            final T casted = entityTypeTest.tryCast(part);
            if (casted != null && (predicate == null || predicate.test(casted))) {
                into.add(casted);
                if (into.size() >= maxCount) {
                    break;
                }
            }
        }
    }

    @Override
    public void entityMove(final Entity entity, final long oldSection, final long newSection) {
    }

    @Override
    public boolean configFixMC224294() {
        return true;
    }

    @Override
    public boolean configFixMC119599(final ServerLevel world) {
        return world.paperConfig().environment.fixMissingTreeSnow;
    }

    @Override
    public boolean configAutoConfigSendDistance() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingAdvanced.autoConfigSendDistance;
    }

    @Override
    public double configPlayerMaxLoadRate() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingBasic.playerMaxChunkLoadRate;
    }

    @Override
    public double configPlayerMaxGenRate() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingBasic.playerMaxChunkGenerateRate;
    }

    @Override
    public double configPlayerMaxSendRate() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingBasic.playerMaxChunkSendRate;
    }

    @Override
    public int configPlayerMaxConcurrentLoads() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingAdvanced.playerMaxConcurrentChunkLoads;
    }

    @Override
    public int configPlayerMaxConcurrentGens() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().chunkLoadingAdvanced.playerMaxConcurrentChunkGenerates;
    }

    @Override
    public long configAutoSaveInterval(final ServerLevel world) {
        return world.paperConfig().chunks.autoSaveInterval.value();
    }

    @Override
    public int configMaxAutoSavePerTick(final ServerLevel world) {
        return world.paperConfig().chunks.maxAutoSaveChunksPerTick;
    }

    @Override
    public int configMinChunkUnloadCount(final ServerLevel world) {
        return world.paperConfig().chunks.minChunkUnloadCount;
    }

    @Override
    public double configMinChunkUnloadFraction(final ServerLevel world) {
        return world.paperConfig().chunks.minChunkUnloadFraction;
    }

    @Override
    public boolean forceNoSave(final ChunkAccess chunk) {
        // CraftBukkit's mustNotSave is enforced by MixinLevelChunk in vanilla's save path
        return false;
    }

    @Override
    public CompoundTag convertNBT(final DSL.TypeReference type, final DataFixer dataFixer, final CompoundTag nbt,
                                  final int fromVersion, final int toVersion) {
        return (CompoundTag) dataFixer.update(type, new Dynamic<>(NbtOps.INSTANCE, nbt), fromVersion, toVersion).getValue();
    }

    @Override
    public boolean hasMainChunkLoadHook() {
        return false;
    }

    @Override
    public void mainChunkLoad(final ChunkAccess chunk, final SerializableChunkData chunkData) {
    }

    @Override
    public List<Entity> modifySavedEntities(final ServerLevel world, final int chunkX, final int chunkZ, final List<Entity> entities) {
        return entities;
    }

    @Override
    public void unloadEntity(final Entity entity) {
        entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
    }

    @Override
    public void postLoadProtoChunk(final ServerLevel world, final ProtoChunk chunk) {
        ChunkStatusTasks.postLoadProtoChunk(world, TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), chunk.getEntities()));
    }

    @Override
    public int modifyEntityTrackingRange(final Entity entity, final int currentRange) {
        return org.spigotmc.TrackingRange.getEntityTrackingRange(entity, currentRange);
    }

    @Override
    public boolean addTicketForEnderPearls(final ServerLevel world) {
        return !world.paperConfig().misc.legacyEnderPearlBehavior;
    }

    // ChunkSystemHooks

    @Override
    public void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run) {
        this.scheduleChunkTask(level, chunkX, chunkZ, run, Priority.NORMAL);
    }

    @Override
    public void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run, final Priority priority) {
        // vanilla runs chunk work on the main thread
        level.getChunkSource().mainThreadProcessor.execute(run);
    }

    @Override
    public void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final boolean gen,
                                  final ChunkStatus toStatus, final boolean addTicket, final Priority priority,
                                  final Consumer<ChunkAccess> onComplete) {
        if (gen) {
            this.scheduleChunkLoad(level, chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
            return;
        }
        // Load the stored chunk without generating anything, then go on to the target status
        // only when the chunk had already reached it; otherwise the answer is null.
        this.scheduleChunkLoad(level, chunkX, chunkZ, ChunkStatus.EMPTY, addTicket, priority, (final ChunkAccess chunk) -> {
            if (chunk == null) {
                onComplete.accept(null);
                return;
            }
            final ChunkStatus status = chunk.getPersistedStatus();
            if (!status.isOrAfter(toStatus)) {
                onComplete.accept(null);
            } else if (status == ChunkStatus.EMPTY || toStatus == ChunkStatus.EMPTY) {
                onComplete.accept(chunk);
            } else {
                this.scheduleChunkLoad(level, chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
            }
        });
    }

    /**
     * Loads or generates the chunk to {@code toStatus} and hands it to {@code onComplete} on the
     * main thread, or null when it cannot be loaded. With {@code addTicket} a plugin ticket keeps
     * the chunk loaded for a while afterwards, so the callback and the code after it can use it.
     */
    @Override
    public void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final ChunkStatus toStatus,
                                  final boolean addTicket, final Priority priority, final Consumer<ChunkAccess> onComplete) {
        final ServerChunkCache cache = level.getChunkSource();
        final Runnable load = () -> {
            if (addTicket) {
                cache.addTicket(new Ticket(TicketType.PLUGIN, ChunkLevel.byStatus(toStatus)), new ChunkPos(chunkX, chunkZ));
            }
            cache.getChunkFuture(chunkX, chunkZ, toStatus, true).whenCompleteAsync((final ChunkResult<ChunkAccess> result, final Throwable thr) -> {
                ChunkAccess chunk = null;
                if (thr != null) {
                    LOGGER.error("Failed to load chunk ({}, {}) in {} to {}", chunkX, chunkZ, level, toStatus, thr);
                } else {
                    chunk = result.orElse(null);
                }
                if (onComplete != null) {
                    onComplete.accept(chunk);
                }
            }, cache.mainThreadProcessor);
        };
        if (Thread.currentThread() == cache.mainThread) {
            load.run();
        } else {
            cache.mainThreadProcessor.execute(load);
        }
    }

    @Override
    public void scheduleTickingState(final ServerLevel level, final int chunkX, final int chunkZ,
                                     final FullChunkStatus toStatus, final boolean addTicket,
                                     final Priority priority, final Consumer<LevelChunk> onComplete) {
        final ServerChunkCache cache = level.getChunkSource();
        final Runnable load = () -> {
            final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            final int ticketLevel = ChunkLevel.byStatus(toStatus);
            if (addTicket) {
                cache.addTicket(new Ticket(TicketType.PLUGIN, ticketLevel), pos);
            } else {
                cache.addTicket(new Ticket(TicketType.UNKNOWN, ticketLevel), pos);
            }
            cache.runDistanceManagerUpdates();
            final ChunkHolder holder = cache.chunkMap.getUpdatingChunkIfPresent(ChunkPos.pack(chunkX, chunkZ));
            if (holder == null) {
                if (onComplete != null) {
                    onComplete.accept(null);
                }
                return;
            }
            final java.util.concurrent.CompletableFuture<ChunkResult<LevelChunk>> future = switch (toStatus) {
                case BLOCK_TICKING -> holder.getTickingChunkFuture();
                case ENTITY_TICKING -> holder.getEntityTickingChunkFuture();
                default -> holder.getFullChunkFuture();
            };
            future.whenCompleteAsync((final ChunkResult<LevelChunk> result, final Throwable thr) -> {
                LevelChunk chunk = null;
                if (thr != null) {
                    LOGGER.error("Failed to bring chunk ({}, {}) in {} to {}", chunkX, chunkZ, level, toStatus, thr);
                } else {
                    chunk = result.orElse(null);
                }
                if (onComplete != null) {
                    onComplete.accept(chunk);
                }
            }, cache.mainThreadProcessor);
        };
        if (Thread.currentThread() == cache.mainThread) {
            load.run();
        } else {
            cache.mainThreadProcessor.execute(load);
        }
    }

    @Override
    public List<ChunkHolder> getVisibleChunkHolders(final ServerLevel level) {
        return new ArrayList<>(level.getChunkSource().chunkMap.visibleChunkMap.values());
    }

    @Override
    public List<ChunkHolder> getUpdatingChunkHolders(final ServerLevel level) {
        return new ArrayList<>(level.getChunkSource().chunkMap.updatingChunkMap.values());
    }

    @Override
    public int getVisibleChunkHolderCount(final ServerLevel level) {
        return level.getChunkSource().chunkMap.visibleChunkMap.size();
    }

    @Override
    public int getUpdatingChunkHolderCount(final ServerLevel level) {
        return level.getChunkSource().chunkMap.updatingChunkMap.size();
    }

    @Override
    public boolean hasAnyChunkHolders(final ServerLevel level) {
        return this.getUpdatingChunkHolderCount(level) != 0;
    }

    @Override
    public boolean screenEntity(final ServerLevel level, final Entity entity, final boolean fromDisk, final boolean event) {
        return true;
    }

    @Override
    public void onChunkHolderCreate(final ServerLevel level, final ChunkHolder holder) {
    }

    @Override
    public void onChunkHolderDelete(final ServerLevel level, final ChunkHolder holder) {
    }

    @Override
    public void onChunkPreBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkNotBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkPostNotBorder(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkNotTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public void onChunkNotEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
    }

    @Override
    public ChunkHolder getUnloadingChunkHolder(final ServerLevel level, final int chunkX, final int chunkZ) {
        return null;
    }

    /** Vanilla's per-player load distance: the client's requested distance, capped by the server's. */
    @Override
    public int getViewDistance(final ServerPlayer player) {
        return Mth.clamp(player.requestedViewDistance(), 2, player.level().getChunkSource().chunkMap.serverViewDistance);
    }

    @Override
    public int getSendViewDistance(final ServerPlayer player) {
        // vanilla sends every chunk it loads for a player, up to the player's API limit
        final int distance = this.getViewDistance(player);
        final int limit = ((org.bukkit.craftbukkit.entity.CraftPlayer) player.getBukkitEntity()).getSendDistanceLimit();
        return limit < 0 ? distance : Math.min(distance, limit);
    }

    @Override
    public int getTickViewDistance(final ServerPlayer player) {
        return Math.min(this.getViewDistance(player), FeatureHooks.getSimulationDistance(player.level()));
    }

    @Override
    public void addPlayerToDistanceMaps(final ServerLevel world, final ServerPlayer player) {
    }

    @Override
    public void removePlayerFromDistanceMaps(final ServerLevel world, final ServerPlayer player) {
    }

    @Override
    public void updateMaps(final ServerLevel world, final ServerPlayer player) {
    }

    @Override
    public long[] getCounterTypesUncached(final TicketType type) {
        return new long[0];
    }
}
