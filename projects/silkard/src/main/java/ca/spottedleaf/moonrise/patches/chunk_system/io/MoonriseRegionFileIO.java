package ca.spottedleaf.moonrise.patches.chunk_system.io;

import java.io.IOException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.EntityStorage;

/**
 * The parts of Moonrise's region file I/O that plugins built against Paper call. SilkPaper keeps
 * vanilla's chunk system, so these flush vanilla's chunk and entity region storage instead.
 */
public final class MoonriseRegionFileIO {

    private MoonriseRegionFileIO() {
    }

    /** Waits for the level's pending chunk and entity writes to finish. */
    public static void flush(final ServerLevel world) {
        world.getChunkSource().chunkMap.synchronize(false).join();
        if (world.entityManager.permanentStorage instanceof EntityStorage entityStorage) {
            entityStorage.flush(false);
        }
    }

    /** Waits for the level's pending writes and flushes its region files to disk. */
    public static void flushRegionStorages(final ServerLevel world) throws IOException {
        world.getChunkSource().chunkMap.synchronize(true).join();
        if (world.entityManager.permanentStorage instanceof EntityStorage entityStorage) {
            entityStorage.flush(true);
        }
    }
}
