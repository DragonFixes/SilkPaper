package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperCommonListenerAccess;
import com.mohistmc.silkard.injected.server.network.ContextServerCommonPacketListenerImpl;
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's shared listener state: client brand, plugin channels, Adventure resource pack callbacks and
 * the cookie connection of the game and configuration listeners.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinPaperServerCommonPacketListenerImpl implements PaperCommonListenerAccess {

    @Shadow @Final protected Connection connection;

    @Unique private final Map<UUID, ResourcePackCallback> paper$packCallbacks = new ConcurrentHashMap<>();

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void paper$recordBrand(final ServerboundCustomPayloadPacket packet, final CallbackInfo ci) {
        if (packet.payload() instanceof final BrandPayload brand) {
            this.connection.paper$clientBrand(brand.brand());
        }
    }

    @Inject(method = "handleResourcePackResponse", at = @At("TAIL"))
    private void paper$packCallback(final ServerboundResourcePackPacket packet, final CallbackInfo ci) {
        final ResourcePackCallback callback = packet.action().isTerminal()
            ? this.paper$packCallbacks.remove(packet.id())
            : this.paper$packCallbacks.get(packet.id());
        if (callback != null) {
            callback.packEventReceived(packet.id(), ResourcePackStatus.valueOf(packet.action().name()), this.paper$audience());
        }
    }

    @Unique
    private Audience paper$audience() {
        final Object self = this;
        if (self instanceof final ServerConfigurationPacketListenerImpl configuration) {
            return configuration.paper$paperConnection();
        }
        final Player player = ((ContextServerCommonPacketListenerImpl) self).getCraftPlayer();
        return player != null ? player : Audience.empty();
    }

    @Override
    public @Nullable String paper$clientBrand() {
        return this.connection.paper$clientBrand();
    }

    @Override
    public Set<String> paper$pluginMessagerChannels() {
        return this.connection.paper$pluginMessagerChannels();
    }

    @Override
    public Map<UUID, ResourcePackCallback> paper$packCallbacks() {
        return this.paper$packCallbacks;
    }

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
