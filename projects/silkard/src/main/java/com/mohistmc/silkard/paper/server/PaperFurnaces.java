package com.mohistmc.silkard.paper.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Paper's furnace helpers that vanilla only has as private statics.
 */
public final class PaperFurnaces {

    private static final int DEFAULT_COOK_TIME = 200;

    private PaperFurnaces() {
    }

    /**
     * Total cook time for the furnace's current input, scaled by its cook speed multiplier.
     * Without a level there is no recipe lookup, so the vanilla default cook time is scaled instead.
     */
    public static int getTotalCookTime(final @Nullable ServerLevel level, final AbstractFurnaceBlockEntity entity) {
        if (level != null) {
            return AbstractFurnaceBlockEntity.getTotalCookTime(level, entity);
        }
        final double multiplier = entity.silkard$cookSpeedMultiplier();
        return multiplier != 1.0D ? (int) Math.ceil(DEFAULT_COOK_TIME / multiplier) : DEFAULT_COOK_TIME;
    }
}
