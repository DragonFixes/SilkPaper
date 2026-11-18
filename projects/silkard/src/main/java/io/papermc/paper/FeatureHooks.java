package io.papermc.paper;

import io.papermc.paper.command.PaperSubcommand;
import io.papermc.paper.world.chunk.ChunkGenerationState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import org.bukkit.Chunk;
import org.jspecify.annotations.Nullable;

/**
 * Entry points through which CraftBukkit and Paper commands reach chunk-system features.
 *
 * <p>Paper backs these with its Moonrise chunk system. Silkard keeps the vanilla chunk system, which
 * Fabric mods such as C2ME and Lithium patch, so these are answered from vanilla state. Features
 * that only exist in Moonrise (per-player view distances, chunk-load debug dumps, the delayed chunk
 * unload of players) are no-ops.
 */
public final class FeatureHooks {

    private FeatureHooks() {
    }

    public static void registerPaperCommands(final Map<Set<String>, PaperSubcommand> commands) {
        // Moonrise adds its chunk debug subcommands here; the vanilla chunk system has none.
    }

    public static void flushAsyncAppenders() {
        // TODO Silkard - Paper flushes its async log appenders here; Silkard's logging setup has none to flush
    }

    public static void dumpAllChunkLoadInfo(final @Nullable MinecraftServer server, final boolean longPrint) {
        // TODO Silkard - Moonrise chunk task dump; the vanilla chunk system has no equivalent
    }

    public static void setPlayerChunkUnloadDelay(final long ticks) {
        // TODO Silkard - delay-chunk-unloads-by needs Moonrise's player chunk loader; the setting is read but unused
    }

    public static Iterable<Entity> getAllEntities(final ServerLevel level) {
        return level.getAllEntities();
    }

    public static Executor getWorldgenExecutor() {
        return Util.backgroundExecutor();
    }

    public static LevelChunkSection createSection(final PalettedContainerFactory factory, final @Nullable Level level, final @Nullable ChunkPos chunkPos, final int chunkSection) {
        return new LevelChunkSection(factory);
    }

    public static PalettedContainer<BlockState> emptyPalettedBlockContainer() {
        return new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
    }

    public static org.bukkit.entity.Entity[] getChunkEntities(final ServerLevel level, final int chunkX, final int chunkZ) {
        final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        final AABB bounds = new AABB(pos.getMinBlockX(), level.getMinY(), pos.getMinBlockZ(), pos.getMaxBlockX() + 1, level.getMaxY() + 1, pos.getMaxBlockZ() + 1);
        final List<org.bukkit.entity.Entity> entities = new ArrayList<>();
        for (final Entity entity : level.getEntities((Entity) null, bounds, entity -> pos.equals(entity.chunkPosition()))) {
            entities.add(entity.getBukkitEntity());
        }
        return entities.toArray(new org.bukkit.entity.Entity[0]);
    }

    public static ChunkGenerationState getChunkGenerationState(final ServerLevel level, final int chunkX, final int chunkZ) {
        return getChunkGenerationStateAsync(level, chunkX, chunkZ).join();
    }

    public static CompletableFuture<ChunkGenerationState> getChunkGenerationStateAsync(final ServerLevel level, final int chunkX, final int chunkZ) {
        final ChunkMap chunkMap = level.getChunkSource().chunkMap;
        final ChunkStatus loaded = chunkMap.getLatestStatus(ChunkPos.pack(chunkX, chunkZ));
        if (loaded == ChunkStatus.FULL) {
            return CompletableFuture.completedFuture(ChunkGenerationState.GENERATED);
        }
        return chunkMap.read(new ChunkPos(chunkX, chunkZ)).thenApply(tag -> tag.map(FeatureHooks::stateOf).orElse(
            loaded == null ? ChunkGenerationState.NOT_GENERATED : ChunkGenerationState.PARTIAL));
    }

    private static ChunkGenerationState stateOf(final CompoundTag tag) {
        if (tag.getIntOr("DataVersion", 0) < SharedConstants.getCurrentVersion().dataVersion().version()) {
            return ChunkGenerationState.OUTDATED;
        }
        final String status = tag.getStringOr("Status", "");
        return "minecraft:full".equals(status) || "full".equals(status) ? ChunkGenerationState.GENERATED : ChunkGenerationState.PARTIAL;
    }

    public static void sendChunkRefreshPackets(final List<ServerPlayer> players, final LevelChunk chunk) {
        final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, chunk.getLevel().getLightEngine(), null, null);
        for (final ServerPlayer player : players) {
            player.connection.send(packet);
        }
    }

    public static int getViewDistance(final ServerLevel level) {
        return level.getChunkSource().chunkMap.serverViewDistance;
    }

    public static int getSimulationDistance(final ServerLevel level) {
        return level.getChunkSource().chunkMap.distanceManager.simulationDistance;
    }

    public static void setViewDistance(final ServerLevel level, final int distance) {
        level.getChunkSource().setViewDistance(distance);
    }

    public static void setSimulationDistance(final ServerLevel level, final int distance) {
        level.getChunkSource().setSimulationDistance(distance);
    }

    public static int getSendViewDistance(final ServerLevel level) {
        return getViewDistance(level);
    }

    public static void setSendViewDistance(final ServerLevel level, final int distance) {
        // TODO Silkard - the vanilla chunk system sends exactly the view distance; a separate send distance needs Moonrise
    }

    public static Set<Long> getSentChunkKeys(final ServerPlayer player) {
        final Set<Long> keys = new HashSet<>();
        player.getChunkTrackingView().forEach(pos -> keys.add(pos.pack()));
        return keys;
    }

    public static Set<Chunk> getSentChunks(final ServerPlayer player) {
        final Set<Chunk> chunks = new HashSet<>();
        final org.bukkit.World world = player.level().getWorld();
        player.getChunkTrackingView().forEach(pos -> chunks.add(world.getChunkAt(pos.x(), pos.z())));
        return chunks;
    }

    public static boolean isChunkSent(final ServerPlayer player, final long chunkKey) {
        return player.getChunkTrackingView().contains(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
    }

    public static void setViewDistance(final ServerPlayer player, final int distance) {
        // TODO Silkard - per-player view distance needs Moonrise's player chunk loader
    }

    public static void setSimulationDistance(final ServerPlayer player, final int distance) {
        // TODO Silkard - per-player simulation distance needs Moonrise's player chunk loader
    }

    public static void setSendViewDistance(final ServerPlayer player, final int distance) {
        // TODO Silkard - per-player send distance needs Moonrise's player chunk loader
    }
}
