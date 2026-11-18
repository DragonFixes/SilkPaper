package com.mohistmc.silkard.injected.paper.paper;

import com.mohistmc.silkard.util.ContextStateException;
import org.bukkit.entity.Mob;

/**
 * Members Paper adds to {@link net.minecraft.world.entity.ai.goal.Goal}.
 */
public interface PaperGoalAccess {

    /** The API view of this vanilla goal, created once and reused so callers see a stable identity. */
    default <T extends Mob> com.destroystokyo.paper.entity.ai.Goal<T> paper$asPaperGoal() {
        throw new ContextStateException("Not implemented");
    }
}
