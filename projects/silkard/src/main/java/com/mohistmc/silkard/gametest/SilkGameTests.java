package com.mohistmc.silkard.gametest;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.TestFunctionLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/**
 * Gameplay tests: runs vanilla GameTest-framework tests inside a full SilkPaper server (CraftBukkit,
 * Paper, Purpur and Leaf code all active), so a test exercises the same code a production server runs.
 * Vanilla's headless {@code net.minecraft.gametest.Main} skips all of that.
 * <p>
 * Inert unless {@code -Dsilkpaper.gametest=<selector>} is set, e.g. {@code silkpaper:*} or {@code *:*}.
 * {@code -Dsilkpaper.gametest.report=<file>} writes a JUnit-style XML report, and
 * {@code -Dsilkpaper.gametest.result=<file>} receives the number of failed required tests (0 = all passed).
 * The tests come from the datapacks in {@code gametests/packs}; {@code scripts\game-test} sets all of this up.
 */
public final class SilkGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PROPERTY = "silkpaper.gametest";
    // Flat world surface, far from spawn so spawn chunks and players never interfere.
    private static final BlockPos ORIGIN = new BlockPos(100_000, -60, 100_000);
    private static boolean started;

    private SilkGameTests() {
    }

    public static boolean enabled() {
        return System.getProperty(PROPERTY) != null;
    }

    /** Runs while the TEST_FUNCTION registry is bootstrapped, before it is frozen. */
    public static void registerFunctions() {
        if (enabled()) {
            TestFunctionLoader.registerLoader(new SilkTestFunctions());
        }
    }

    /** Runs once all worlds are loaded and POSTWORLD plugins are enabled. */
    public static void start(final MinecraftServer server) {
        if (!enabled() || started) {
            return;
        }
        started = true;
        final String selection = System.getProperty(PROPERTY);
        final List<Holder.Reference<GameTestInstance>> tests = GameTestServer.getTestsForSelection(server.registryAccess(), selection)
            .filter(test -> !test.value().manualOnly())
            .toList();
        final int total = server.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE).size();
        if (tests.isEmpty()) {
            LOGGER.error("[SilkGameTests] selector '{}' matched no tests ({} registered); is the test datapack in world/datapacks?", selection, total);
            finish(server, 1);
            return;
        }
        final String report = System.getProperty(PROPERTY + ".report");
        if (report != null) {
            try {
                GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(new File(report)));
            } catch (final Exception e) {
                LOGGER.error("[SilkGameTests] could not create report {}", report, e);
            }
        }
        final ServerLevel level = server.overworld();
        final GameTestRunner runner = GameTestRunner.Builder.fromBatches(GameTestBatchFactory.divideIntoBatches(tests, GameTestBatchFactory.DIRECT, server), server)
            .newStructureSpawner(new StructureGridSpawner(ignored -> ORIGIN, 8, false))
            .build();
        final Collection<GameTestInfo> infos = runner.getTestInfos();
        final MultipleTestTracker tracker = new MultipleTestTracker(infos);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        tracker.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(final GameTestInfo testInfo) {
            }

            @Override
            public void testPassed(final GameTestInfo testInfo, final GameTestRunner runner) {
                LOGGER.info("[SilkGameTests] PASS {}", testInfo.id());
                this.check();
            }

            @Override
            public void testFailed(final GameTestInfo testInfo, final GameTestRunner runner) {
                // Logged at WARN, so the server log's ERROR lines stay reserved for server problems.
                LOGGER.warn("[SilkGameTests] FAIL {}{}: {}", testInfo.id(), testInfo.isRequired() ? "" : " (optional)",
                    testInfo.getError() == null ? "?" : testInfo.getError().getDescription().getString());
                this.check();
            }

            @Override
            public void testAddedForRerun(final GameTestInfo original, final GameTestInfo copy, final GameTestRunner runner) {
            }

            private void check() {
                if (!tracker.isDone()) {
                    return;
                }
                GlobalTestReporter.finish();
                LOGGER.info("[SilkGameTests] ===== {} tests done in {}: {} required failed, {} optional failed =====",
                    tracker.getTotalCount(), stopwatch.stop(), tracker.getFailedRequiredCount(), tracker.getFailedOptionalCount());
                finish(server, tracker.getFailedRequiredCount());
            }
        });
        LOGGER.info("[SilkGameTests] running {} of {} registered tests matching '{}' at {} in {}", infos.size(), total, selection,
            ORIGIN.toShortString(), level.dimension().identifier());
        runner.start();
    }

    private static void finish(final MinecraftServer server, final int failed) {
        // The shutdown path exits the JVM with its own status, so the script reads this file instead.
        final String result = System.getProperty(PROPERTY + ".result");
        if (result != null) {
            try {
                Files.writeString(Path.of(result), Integer.toString(failed));
            } catch (final IOException e) {
                LOGGER.error("[SilkGameTests] could not write result file {}", result, e);
            }
        }
        server.halt(false);
    }
}
