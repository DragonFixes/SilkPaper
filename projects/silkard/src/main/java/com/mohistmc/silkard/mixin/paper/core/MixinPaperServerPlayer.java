package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper login time and the Purpur AFK flag on {@link ServerPlayer}. A ServerPlayer is created for
 * every login, so the login time is the creation time of the object that joined.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinPaperServerPlayer implements PaperServerPlayer {

    @Unique private long paper$loginTime = System.currentTimeMillis();
    @Unique private boolean paper$afk;

    @Override
    public long paper$loginTime() {
        return this.paper$loginTime;
    }

    @Override
    public boolean paper$afk() {
        return this.paper$afk;
    }

    @Override
    public void paper$setAfk(boolean afk) {
        // TODO Silkard - Purpur's AFK system (idle detection, broadcasts, PlayerAFKEvent, tab suffix) is not ported; this is plain state.
        this.paper$afk = afk;
    }
}
