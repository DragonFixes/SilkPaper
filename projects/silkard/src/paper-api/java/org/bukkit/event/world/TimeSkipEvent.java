package org.bukkit.event.world;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the time skips in a world.
 * <p>
 * If the event is cancelled the time will not change.
 *
 * @see ClockTimeSkipEvent for changing of clocks that affect all worlds
 */
public class TimeSkipEvent extends ClockTimeSkipEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final World world;

    @ApiStatus.Internal
    public TimeSkipEvent(@NotNull World world, @NotNull ClockTimeSkipEvent.SkipReason skipReason, long skipAmount) { // Silkard - qualified, TimeSkipEvent.SkipReason is the Spigot enum
        super(skipReason, skipAmount);
        this.world = world;
    }

    /**
     * Returns the world that time is skipped in.
     *
     * @return world that time is skipped in
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    // Silkard start - Spigot API compat
    /**
     * An enum specifying the reason the time skipped.
     */
    public enum SkipReason {

        /**
         * When time is changed using the vanilla /time command.
         */
        COMMAND,
        /**
         * When time is changed by a plugin.
         */
        CUSTOM,
        /**
         * When time is changed by all players sleeping in their beds and the
         * night skips.
         */
        NIGHT_SKIP
    }
    // Silkard end

    // Silkard start - Spigot API compat
    /**
     * @param world the world
     * @param skipReason the reason for the skip
     * @param skipAmount the amount of time skipped
     */
    public TimeSkipEvent(@NotNull World world, @NotNull SkipReason skipReason, long skipAmount) {
        this(world, ClockTimeSkipEvent.SkipReason.valueOf(skipReason.name()), skipAmount);
    }
    // Silkard end

}
