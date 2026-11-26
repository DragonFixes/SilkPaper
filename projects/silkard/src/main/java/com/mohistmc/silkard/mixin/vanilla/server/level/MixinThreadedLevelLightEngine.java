package com.mohistmc.silkard.mixin.vanilla.server.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper's chunk relight entry point, {@code starlight$serverRelightChunks}, on the vanilla light
 * engine. Paper gets it from Moonrise's Starlight; plugins such as AxiomPaper call it directly on
 * {@link ThreadedLevelLightEngine} after writing blocks straight into chunk sections.
 * <p>
 * Vanilla has no bulk relight, so each chunk is rechecked block by block: one light-thread task
 * per chunk refreshes every section's empty flag and runs {@code checkBlock} on every position,
 * which recomputes both block and sky light, including light flowing across chunk borders.
 * Viewers then get a light update packet for the chunk.
 */
@Mixin(ThreadedLevelLightEngine.class)
public abstract class MixinThreadedLevelLightEngine extends LevelLightEngine {

    private static final int SILKPAPER_RELIGHT_BATCH = 8;

    @Shadow
    @Final
    private ChunkMap chunkMap;

    @Shadow
    private void addTask(final int chunkX, final int chunkZ, final ThreadedLevelLightEngine.TaskType type, final Runnable runnable) {
    }

    @Shadow
    public abstract void tryScheduleUpdate();

    private MixinThreadedLevelLightEngine(final LightChunkGetter chunkSource, final boolean hasBlockLight, final boolean hasSkyLight) {
        super(chunkSource, hasBlockLight, hasSkyLight);
    }

    /**
     * Relights the loaded, already-lit chunks among {@code chunks0}.
     *
     * @param chunkLightCallback called on the main thread for each chunk once it is relit
     * @param onComplete called on the main thread with the number of relit chunks once all are done
     * @return the number of chunks queued for relighting
     */
    public int starlight$serverRelightChunks(final Collection<ChunkPos> chunks0,
                                             final Consumer<ChunkPos> chunkLightCallback,
                                             final IntConsumer onComplete) {
        final List<LevelChunk> chunks = new ArrayList<>();
        if (!chunks0.isEmpty()) {
            final ServerLevel level = this.chunkMap.level;
            for (final ChunkPos pos : new LinkedHashSet<>(chunks0)) {
                final LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
                if (chunk != null && chunk.isLightCorrect()) {
                    chunks.add(chunk);
                }
            }
        }

        if (chunks.isEmpty()) {
            if (onComplete != null) {
                onComplete.accept(0);
            }
            return 0;
        }

        this.silkpaper$relightBatch(chunks, 0, chunkLightCallback, onComplete);
        this.tryScheduleUpdate();
        return chunks.size();
    }

    /**
     * Queues chunks {@code [start, start + SILKPAPER_RELIGHT_BATCH)}. The light thread runs every queued task
     * and then all light updates in one pass, so the next batch is queued only once this one is
     * done, keeping a large paste from flooding the shared light executor.
     */
    private void silkpaper$relightBatch(final List<LevelChunk> chunks, final int start,
                                        final Consumer<ChunkPos> chunkLightCallback, final IntConsumer onComplete) {
        final ServerLevel level = this.chunkMap.level;
        final int end = Math.min(start + SILKPAPER_RELIGHT_BATCH, chunks.size());
        final AtomicInteger remaining = new AtomicInteger(end - start);
        for (int i = start; i < end; i++) {
            final LevelChunk chunk = chunks.get(i);
            final ChunkPos pos = chunk.getPos();
            this.addTask(pos.x(), pos.z(), ThreadedLevelLightEngine.TaskType.PRE_UPDATE, () -> this.silkpaper$recheckChunk(chunk));
            this.addTask(pos.x(), pos.z(), ThreadedLevelLightEngine.TaskType.POST_UPDATE, () -> {
                final boolean batchDone = remaining.decrementAndGet() == 0;
                level.getServer().execute(() -> {
                    this.silkpaper$sendLight(pos);
                    if (chunkLightCallback != null) {
                        chunkLightCallback.accept(pos);
                    }
                    if (batchDone && end == chunks.size() && onComplete != null) {
                        onComplete.accept(chunks.size());
                    }
                });
                if (batchDone && end < chunks.size()) {
                    this.silkpaper$relightBatch(chunks, end, chunkLightCallback, onComplete);
                    this.tryScheduleUpdate();
                }
            });
        }
    }

    // Runs on the light thread. super.* calls the engines directly instead of queueing another task.
    private void silkpaper$recheckChunk(final LevelChunk chunk) {
        final ChunkPos pos = chunk.getPos();
        final LevelChunkSection[] sections = chunk.getSections();
        for (int index = 0; index < sections.length; index++) {
            final int sectionY = this.levelHeightAccessor.getSectionYFromSectionIndex(index);
            super.updateSectionStatus(SectionPos.of(pos, sectionY), sections[index].hasOnlyAir());
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int minY = this.levelHeightAccessor.getMinY();
        final int maxY = this.levelHeightAccessor.getMaxY();
        final int baseX = pos.getMinBlockX();
        final int baseZ = pos.getMinBlockZ();
        for (int y = minY; y <= maxY; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    super.checkBlock(cursor.set(baseX + x, y, baseZ + z));
                }
            }
        }
    }

    private void silkpaper$sendLight(final ChunkPos pos) {
        final List<ServerPlayer> players = this.chunkMap.getPlayers(pos, false);
        if (players.isEmpty()) {
            return;
        }
        final ClientboundLightUpdatePacket packet = new ClientboundLightUpdatePacket(pos, (ThreadedLevelLightEngine) (Object) this, null, null);
        for (final ServerPlayer player : players) {
            if (player.connection != null) {
                player.connection.send(packet);
            }
        }
    }
}
