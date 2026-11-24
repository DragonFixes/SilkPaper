package io.papermc.paper;

import net.minecraft.server.level.ServerLevel;

/**
 * Paper's hooks for code that differs between Paper's chunk system and vanilla's. Plugins built
 * against Paper call these; SilkPaper keeps vanilla's entity manager, so they use it directly.
 */
public final class FeatureHooks {

    private FeatureHooks() {
    }

    public static void tickEntityManager(final ServerLevel level) {
        level.entityManager.tick();
    }

    public static void closeEntityManager(final ServerLevel level, final boolean save) {
        try {
            level.entityManager.close(save);
        } catch (final java.io.IOException exception) {
            throw new RuntimeException("Failed to close entity manager", exception);
        }
    }
}
