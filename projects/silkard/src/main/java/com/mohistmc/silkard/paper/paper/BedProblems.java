package com.mohistmc.silkard.paper.paper;

import net.minecraft.world.entity.player.Player;

/**
 * Sleeping problems Paper adds to {@link Player.BedSleepingProblem}. Compare by identity.
 */
public final class BedProblems {

    /** The bed exploded because sleeping is not possible in this dimension; it carries no message. */
    public static final Player.BedSleepingProblem EXPLOSION = new Player.BedSleepingProblem(null);

    private BedProblems() {
    }
}
