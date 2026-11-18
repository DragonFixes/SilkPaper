package com.mohistmc.silkard.injected.paper.core;

import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

/**
 * Living-entity state that Paper and Purpur add to {@link net.minecraft.world.entity.LivingEntity}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperLivingEntity}.
 */
public interface PaperLivingEntity {

    /** Friction override; {@link TriState#NOT_SET} follows vanilla's discard-friction flag. */
    default TriState paper$frictionState() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setFrictionState(TriState state) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur daylight burning override; {@code null} keeps the mob's vanilla behavior. */
    default @Nullable Boolean paper$shouldBurnInDayOverride() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setShouldBurnInDayOverride(@Nullable Boolean shouldBurnInDay) {
        throw new IllegalStateException("Not implemented");
    }
}
