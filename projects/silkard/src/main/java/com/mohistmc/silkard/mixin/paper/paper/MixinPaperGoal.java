package com.mohistmc.silkard.mixin.paper.paper;

import com.destroystokyo.paper.entity.ai.PaperGoal;
import com.mohistmc.silkard.injected.paper.paper.PaperGoalAccess;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.entity.Mob;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the Paper mob goal API wrapper on each vanilla goal.
 */
@Mixin(Goal.class)
public abstract class MixinPaperGoal implements PaperGoalAccess {

    @Unique private @Nullable PaperGoal<?> paper$paperGoal;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Mob> com.destroystokyo.paper.entity.ai.Goal<T> paper$asPaperGoal() {
        if (this.paper$paperGoal == null) {
            this.paper$paperGoal = new PaperGoal<>((Goal) (Object) this);
        }
        return (com.destroystokyo.paper.entity.ai.Goal<T>) this.paper$paperGoal;
    }
}
