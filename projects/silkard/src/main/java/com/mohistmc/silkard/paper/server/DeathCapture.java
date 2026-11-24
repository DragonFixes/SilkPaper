package com.mohistmc.silkard.paper.server;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Paper - Restore vanilla drops behavior. Implemented on {@code LivingEntity}. While an entity
 * dies, the items it would drop are captured as {@link DefaultDrop}s instead of spawned, so the
 * death event can show and change them; work that must wait for the event, such as dropping the
 * captured items and experience, is queued as post-death tasks.
 */
public interface DeathCapture {

    /** The drops being captured, or {@code null} outside a death. */
    @Nullable List<DefaultDrop> silkard$deathDropItems();

    /** Starts ({@code drops} non-null) or stops capturing drops. */
    void silkard$setDeathDropItems(@Nullable List<DefaultDrop> drops);

    /** Tasks run after the death event, unless a plugin cancels the death. */
    List<Runnable> silkard$postDeathEventTasks();
}
