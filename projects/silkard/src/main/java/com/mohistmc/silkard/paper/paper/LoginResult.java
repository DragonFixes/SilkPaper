package com.mohistmc.silkard.paper.paper;

import net.minecraft.network.chat.Component;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jspecify.annotations.Nullable;

/**
 * Outcome of the login checks (ban, whitelist, full server) that Paper keeps as
 * {@code PlayerList.LoginResult}: a kick message and the matching legacy login event result.
 */
public record LoginResult(@Nullable Component message, PlayerLoginEvent.Result result) {

    public static final LoginResult ALLOW = new LoginResult(null, PlayerLoginEvent.Result.ALLOWED);

    public boolean isAllowed() {
        return this == ALLOW;
    }
}
