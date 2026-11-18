package com.mohistmc.silkard.paper.server;

import net.minecraft.network.chat.Component;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jspecify.annotations.Nullable;

/**
 * Paper's {@code PlayerList.LoginResult}: whether a login may go ahead, and if not, the kick
 * message and the legacy {@link PlayerLoginEvent} result that goes with it.
 */
@SuppressWarnings("deprecation") // PlayerLoginEvent.Result is the legacy login event's own result type
public record LoginResult(@Nullable Component message, PlayerLoginEvent.Result result) {

    public static final LoginResult ALLOW = new LoginResult(null, PlayerLoginEvent.Result.ALLOWED);

    public boolean isAllowed() {
        return this == ALLOW;
    }
}
