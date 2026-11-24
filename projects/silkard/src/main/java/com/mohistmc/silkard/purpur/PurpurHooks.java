package com.mohistmc.silkard.purpur;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import org.purpurmc.purpur.PurpurWorldConfig;

/**
 * Finds the level whose Purpur config applies to a block or fluid callback. Vanilla passes these
 * callbacks a {@link BlockGetter} or {@link LevelReader}, which may be a level, a world generation
 * region or a plain block reader. Purpur reads the config through the world border's level and
 * keeps vanilla behaviour when there is none, so callers treat {@code null} as "use vanilla".
 */
public final class PurpurHooks {

    private PurpurHooks() {
    }

    public static @Nullable Level level(@Nullable BlockGetter getter) {
        if (getter instanceof Level level) return level;
        if (getter instanceof ServerLevelAccessor accessor) return accessor.getLevel();
        if (getter instanceof LevelReader reader) return reader.getWorldBorder().silkard$world();
        return null;
    }

    public static @Nullable PurpurWorldConfig config(@Nullable BlockGetter getter) {
        Level level = level(getter);
        return level == null ? null : level.purpurConfig();
    }
}
