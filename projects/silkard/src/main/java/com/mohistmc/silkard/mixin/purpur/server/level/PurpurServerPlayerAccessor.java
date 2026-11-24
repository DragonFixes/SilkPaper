package com.mohistmc.silkard.mixin.purpur.server.level;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Resets the experience last sent to the client, so the next tick resends the real value. */
@Mixin(ServerPlayer.class)
public interface PurpurServerPlayerAccessor {

    @Accessor("lastSentExp")
    void purpur$setLastSentExp(int lastSentExp);
}
