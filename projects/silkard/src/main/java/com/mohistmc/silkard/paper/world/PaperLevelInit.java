package com.mohistmc.silkard.paper.world;

import io.papermc.paper.world.PaperWorldLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;

/**
 * The arguments Paper's {@code ServerLevel} constructor takes on top of vanilla's, handed to the
 * vanilla constructor it delegates to.
 *
 * <p>Paper's constructor ({@code MixinServerLevel}, a {@code @CreateConstructor}) must call
 * vanilla's constructor before it can touch {@code this}, but vanilla's constructor already needs
 * some of this data: the world's own {@link WorldGenSettings} (vanilla reads the server's) and the
 * {@link SavedDataStorage} that holds Paper's level overrides (vanilla's {@code ServerChunkCache}
 * opens its own). Redirects in those constructors read the value set here for the dimension being
 * built, on the thread building it.</p>
 */
public record PaperLevelInit(
    ResourceKey<Level> dimension,
    WorldGenSettings worldGenSettings,
    ResourceKey<LevelStem> typeKey,
    org.bukkit.World.Environment environment,
    org.bukkit.generator.@Nullable ChunkGenerator generator,
    org.bukkit.generator.@Nullable BiomeProvider biomeProvider,
    SavedDataStorage savedDataStorage,
    PaperWorldLoader.LoadedWorldData loadedWorldData
) {

    private static final ThreadLocal<PaperLevelInit> CURRENT = new ThreadLocal<>();

    public static void begin(PaperLevelInit init) {
        CURRENT.set(init);
    }

    public static void end() {
        CURRENT.remove();
    }

    /**
     * The init data for {@code dimension}, if a Paper-style level for it is being constructed on
     * this thread. Matching on the dimension keeps a stale value (left by a constructor that threw)
     * from leaking into a later, unrelated level.
     */
    public static @Nullable PaperLevelInit current(ResourceKey<Level> dimension) {
        PaperLevelInit init = CURRENT.get();
        return init != null && init.dimension().equals(dimension) ? init : null;
    }
}
