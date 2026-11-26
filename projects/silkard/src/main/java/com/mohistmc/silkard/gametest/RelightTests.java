package com.mohistmc.silkard.gametest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Paper's {@code starlight$serverRelightChunks}, which AxiomPaper calls on
 * {@link ThreadedLevelLightEngine} after writing blocks straight into chunk sections. Blocks are
 * written the same way here, so the light engine never hears of them until the relight runs.
 * Coordinates are relative to {@code silkpaper:platform} (stone floor at y=0).
 */
public final class RelightTests {

    private static final BlockPos LAMP = new BlockPos(4, 1, 4);
    private static final BlockPos BESIDE = new BlockPos(5, 1, 4);

    private RelightTests() {
    }

    public static void relightsDirectSectionWrites(final GameTestHelper helper) {
        final Method relight;
        try {
            relight = ThreadedLevelLightEngine.class.getMethod("starlight$serverRelightChunks", Collection.class, Consumer.class, IntConsumer.class);
        } catch (final NoSuchMethodException e) {
            throw helper.assertionException("ThreadedLevelLightEngine has no public starlight$serverRelightChunks");
        }

        // A stone shell keeps out light from tests running next to this one.
        for (int x = 3; x <= 6; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = 3; z <= 5; z++) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    if (!pos.equals(LAMP) && !pos.equals(BESIDE)) {
                        helper.setBlock(pos, Blocks.STONE);
                    }
                }
            }
        }
        writeDirect(helper, LAMP, Blocks.GLOWSTONE.defaultBlockState());
        helper.assertTrue(blockLight(helper, BESIDE) == 0, "light changed before the relight");

        // Callbacks run as main-thread tasks outside the test's context, so they record what they
        // saw and succeedWhen does the asserting.
        final AtomicInteger phase = new AtomicInteger();
        final AtomicInteger litAfterAdd = new AtomicInteger(-1);
        final AtomicInteger counts = new AtomicInteger();
        relight(helper, relight, counts, () -> {
            litAfterAdd.set(blockLight(helper, BESIDE));
            writeDirect(helper, LAMP, Blocks.AIR.defaultBlockState());
            relight(helper, relight, counts, () -> phase.set(2));
            phase.set(1);
        });

        helper.succeedWhen(() -> {
            helper.assertTrue(phase.get() == 2, "relight not finished");
            helper.assertTrue(counts.get() == 2, "expected 2 relights of 1 chunk each, " + counts.get() + " matched");
            helper.assertTrue(litAfterAdd.get() == 14, "expected block light 14 beside glowstone after relight, found " + litAfterAdd.get());
            final int dark = blockLight(helper, BESIDE);
            helper.assertTrue(dark == 0, "expected block light 0 after removing glowstone and relighting, found " + dark);
        });
    }

    private static void relight(final GameTestHelper helper, final Method relight, final AtomicInteger counts, final Runnable done) {
        final ChunkPos chunk = ChunkPos.containing(helper.absolutePos(LAMP));
        final AtomicInteger relit = new AtomicInteger();
        final IntConsumer onComplete = count -> {
            if (count == 1 && relit.get() == 1) {
                counts.incrementAndGet();
            }
            done.run();
        };
        final Object queued;
        try {
            queued = relight.invoke(helper.getLevel().getChunkSource().getLightEngine(),
                List.of(chunk), (Consumer<ChunkPos>) pos -> relit.incrementAndGet(), onComplete);
        } catch (final ReflectiveOperationException e) {
            throw helper.assertionException("relight threw " + e.getCause());
        }
        helper.assertTrue(((Integer) queued) == 1, "expected 1 queued chunk, got " + queued);
    }

    private static void writeDirect(final GameTestHelper helper, final BlockPos relative, final BlockState state) {
        final BlockPos pos = helper.absolutePos(relative);
        final LevelChunk chunk = helper.getLevel().getChunkAt(pos);
        chunk.getSection(chunk.getSectionIndex(pos.getY())).setBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
    }

    private static int blockLight(final GameTestHelper helper, final BlockPos relative) {
        return helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(relative));
    }
}
