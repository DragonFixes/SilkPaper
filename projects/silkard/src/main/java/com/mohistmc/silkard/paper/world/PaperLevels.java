package com.mohistmc.silkard.paper.world;

import io.papermc.paper.world.PaperWorldLoader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;

/**
 * Calls Paper's {@code ServerLevel} constructor. {@code MixinServerLevel} adds it at runtime, so
 * SilkPaper's own sources, compiled against vanilla, cannot name it directly.
 */
public final class PaperLevels {

    private static final MethodHandle CONSTRUCTOR;

    static {
        try {
            CONSTRUCTOR = MethodHandles.publicLookup().findConstructor(ServerLevel.class, MethodType.methodType(void.class,
                MinecraftServer.class, Executor.class, LevelStorageSource.LevelStorageAccess.class, WorldGenSettings.class,
                ResourceKey.class, LevelStem.class, boolean.class, long.class, List.class, boolean.class,
                ResourceKey.class, org.bukkit.World.Environment.class, org.bukkit.generator.ChunkGenerator.class,
                org.bukkit.generator.BiomeProvider.class, SavedDataStorage.class, PaperWorldLoader.LoadedWorldData.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private PaperLevels() {
    }

    public static ServerLevel create(
        MinecraftServer server,
        Executor executor,
        LevelStorageSource.LevelStorageAccess levelStorage,
        WorldGenSettings worldGenSettings,
        ResourceKey<Level> dimension,
        LevelStem levelStem,
        boolean isDebug,
        long biomeZoomSeed,
        List<CustomSpawner> customSpawners,
        boolean tickTime,
        ResourceKey<LevelStem> typeKey,
        org.bukkit.World.Environment environment,
        org.bukkit.generator.@Nullable ChunkGenerator generator,
        org.bukkit.generator.@Nullable BiomeProvider biomeProvider,
        SavedDataStorage savedDataStorage,
        PaperWorldLoader.LoadedWorldData loadedWorldData
    ) {
        try {
            return (ServerLevel) CONSTRUCTOR.invoke(server, executor, levelStorage, worldGenSettings, dimension, levelStem, isDebug,
                biomeZoomSeed, customSpawners, tickTime, typeKey, environment, generator, biomeProvider, savedDataStorage, loadedWorldData);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }
}
