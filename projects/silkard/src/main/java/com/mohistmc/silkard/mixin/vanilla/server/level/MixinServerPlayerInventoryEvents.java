package com.mohistmc.silkard.mixin.vanilla.server.level;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.bukkit.InventoryCloseReasonHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit's inventory open and close events for containers the server opens. Opening a
 * container fires InventoryOpenEvent, which closes any container already open and can cancel or
 * retitle the new one. Closing fires InventoryCloseEvent with Paper's close reason.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerInventoryEvents implements InventoryCloseReasonHolder {

    @Unique private InventoryCloseEvent.Reason silkard$closeReason = InventoryCloseEvent.Reason.UNKNOWN;
    @Unique private net.kyori.adventure.text.@Nullable Component silkard$titleOverride;

    @Override
    public void silkard$closeContainer(InventoryCloseEvent.Reason reason) {
        this.silkard$closeReason = reason;
        ((ServerPlayer) (Object) this).closeContainer();
    }

    @Inject(method = "closeContainer", at = @At("HEAD"))
    private void silkard$fireCloseEvent(CallbackInfo ci) {
        InventoryCloseEvent.Reason reason = this.silkard$closeReason;
        this.silkard$closeReason = InventoryCloseEvent.Reason.UNKNOWN;
        CraftEventFactory.handleInventoryCloseEvent((ServerPlayer) (Object) this, reason); // CraftBukkit
    }

    // Paper - Inventory close reason: a container that is no longer valid closes as CANT_USE
    @WrapOperation(method = {"tick", "doTick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"), require = 2)
    private void silkard$closeInvalid(ServerPlayer player, Operation<Void> original) {
        this.silkard$closeReason = InventoryCloseEvent.Reason.CANT_USE;
        original.call(player);
    }

    // CraftBukkit - SPIGOT-6552: InventoryOpenEvent closes the open container itself
    @WrapWithCondition(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
    private boolean silkard$deferClose(ServerPlayer player) {
        return false;
    }

    @WrapOperation(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private @Nullable AbstractContainerMenu silkard$fireOpenEvent(MenuProvider provider, int containerId, Inventory inventory, Player player, Operation<AbstractContainerMenu> original) {
        this.silkard$titleOverride = null;
        AbstractContainerMenu menu = original.call(provider, containerId, inventory, player);
        if (menu == null) {
            return null;
        }
        menu.setTitle(provider.getDisplayName());
        ServerPlayer self = (ServerPlayer) (Object) this;
        var result = CraftEventFactory.callInventoryOpenEventWithTitle(self, menu, false); // Paper - Add titleOverride to InventoryOpenEvent
        if (result.getSecond() == null) {
            // SPIGOT-5263, SPIGOT-5355: a cancelled open leaves the chest closed
            if (provider instanceof Container container) {
                container.stopOpen(self);
            } else if (provider instanceof org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest.Provider doubleChest) {
                doubleChest.container.stopOpen(self);
            }
            return null;
        }
        this.silkard$titleOverride = result.getFirst();
        return result.getSecond();
    }

    @ModifyArg(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundOpenScreenPacket;<init>(ILnet/minecraft/world/inventory/MenuType;Lnet/minecraft/network/chat/Component;)V"), index = 2)
    private Component silkard$openTitle(Component title) {
        net.kyori.adventure.text.Component override = this.silkard$titleOverride;
        this.silkard$titleOverride = null;
        return override != null ? io.papermc.paper.adventure.PaperAdventure.asVanilla(override) : title;
    }
}
