package com.mohistmc.silkard.mixin.vanilla.world.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Objects;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TicketStorage.class)
public abstract class MixinTicketStorage {

    // Paper - add identifier: tickets of the same type and level are only the same ticket when
    // they share an identifier, so plugin chunk tickets from different plugins on one chunk are
    // added, refreshed and removed independently.
    @ModifyReturnValue(method = "isTicketSameTypeAndLevel", at = @At("RETURN"))
    private static boolean silkard$compareIdentifier(boolean same, Ticket ticket, Ticket t) {
        return same && Objects.equals(ticket.silkard_key(), t.silkard_key());
    }
}
