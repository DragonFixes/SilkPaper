package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperCommonListenerAccess;
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Routes cookie responses to the Paper connection of the game or configuration listener.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinPaperServerCommonPacketListenerImpl implements PaperCommonListenerAccess {

    @Override
    public @Nullable ReadablePlayerCookieConnectionImpl paper$cookieConnection() {
        final Object self = this;
        if (self instanceof final ServerGamePacketListenerImpl game) {
            return game.paper$playerGameConnection();
        }
        if (self instanceof final ServerConfigurationPacketListenerImpl configuration) {
            return configuration.paper$paperConnection();
        }
        return null;
    }
}
