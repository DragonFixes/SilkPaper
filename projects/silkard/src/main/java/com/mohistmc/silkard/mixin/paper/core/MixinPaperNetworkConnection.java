package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperNetworkConnection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.minecraft.network.Connection;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper's plugin channel, client brand and resource pack state, kept per connection. */
@Mixin(Connection.class)
public abstract class MixinPaperNetworkConnection implements PaperNetworkConnection {

    @Unique private final Set<String> paper$pluginMessagerChannels = ConcurrentHashMap.newKeySet();
    @Unique private final Map<UUID, ResourcePackCallback> paper$packCallbacks = new ConcurrentHashMap<>();
    @Unique private volatile @Nullable String paper$clientBrand;
    @Unique private volatile PlayerResourcePackStatusEvent.@Nullable Status paper$resourcePackStatus;

    @Override
    public Set<String> paper$pluginMessagerChannels() {
        return this.paper$pluginMessagerChannels;
    }

    @Override
    public @Nullable String paper$clientBrand() {
        return this.paper$clientBrand;
    }

    @Override
    public void paper$setClientBrand(@Nullable String brand) {
        this.paper$clientBrand = brand;
    }

    @Override
    public Map<UUID, ResourcePackCallback> paper$packCallbacks() {
        return this.paper$packCallbacks;
    }

    @Override
    public PlayerResourcePackStatusEvent.@Nullable Status paper$resourcePackStatus() {
        return this.paper$resourcePackStatus;
    }

    @Override
    public void paper$setResourcePackStatus(PlayerResourcePackStatusEvent.@Nullable Status status) {
        this.paper$resourcePackStatus = status;
    }
}
