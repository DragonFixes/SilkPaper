package com.mohistmc.silkard.mixin.vanilla.network.protocol.game;

import io.izzel.arclight.mixin.CreateConstructor;
import io.izzel.arclight.mixin.ShadowConstructor;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Paper's extra constructors that take prepared entries (Add Listing API for Player). Plugins
 * compiled against Paper, such as TAB, call them directly.
 */
@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public abstract class MixinClientboundPlayerInfoUpdatePacket {

    @Shadow @Final @Mutable private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

    @ShadowConstructor
    public void silkard$constructor(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, Collection<ServerPlayer> players) {
        throw new AbstractMethodError();
    }

    @CreateConstructor
    public void silkard$constructor(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        this.silkard$constructor(actions, List.<ServerPlayer>of());
        this.entries = entries;
    }

    @CreateConstructor
    public void silkard$constructor(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry) {
        this.silkard$constructor(actions, List.<ServerPlayer>of());
        this.entries = List.of(entry);
    }
}
