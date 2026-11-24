package com.mohistmc.silkard.mixin.vanilla.server.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Prediction;
import net.minecraft.util.TickThrottler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftInput;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit/Paper events fired from the player's own input packets: PlayerInputEvent,
 * PlayerToggleSneakEvent, PlayerToggleSprintEvent, PlayerToggleFlightEvent, PlayerItemHeldEvent,
 * PlayerSwapHandItemsEvent,
 * PlayerArmSwingEvent (a PlayerAnimationEvent), PlayerInteractEvent for left clicks that hit
 * nothing, and InventoryCreativeEvent. Cancelling any of them leaves the server state unchanged and
 * resyncs the client where it already applied the change.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerPlayerEvents {

    // @formatter:off
    @Shadow public ServerPlayer player;
    @Shadow @Final private TickThrottler dropSpamThrottler;
    @Shadow public abstract boolean hasClientLoaded();
    // @formatter:on

    /** Whether the shift state in the input packet being handled may be applied. */
    @Unique
    private boolean silkard$applyShift = true;

    private static final String ENSURE_THREAD = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V";

    // ---- PlayerInputEvent, PlayerToggleSneakEvent

    @Inject(method = "handlePlayerInput", require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$inputEvents(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        Input lastInput = this.player.getLastClientInput();
        if (!packet.input().equals(lastInput)) {
            new PlayerInputEvent(this.player.getBukkitEntity(), new CraftInput(packet.input())).callEvent();
        }
        this.silkard$applyShift = true;
        if (lastInput.shift() != packet.input().shift()) {
            this.silkard$applyShift = new PlayerToggleSneakEvent(this.player.getBukkitEntity(), packet.input().shift()).callEvent();
        }
    }

    @WrapOperation(method = "handlePlayerInput", require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V"))
    private void silkard$sneakCancelled(ServerPlayer player, boolean shift, Operation<Void> original) {
        if (this.silkard$applyShift) {
            original.call(player, shift);
        }
    }

    // ---- PlayerToggleSprintEvent

    @Inject(method = "handlePlayerCommand", cancellable = true, require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void silkard$sprintEvent(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        if (this.player.isRemoved()) {
            ci.cancel();
            return;
        }
        ServerboundPlayerCommandPacket.Action action = packet.getAction();
        if (action == ServerboundPlayerCommandPacket.Action.START_SPRINTING || action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            if (!new PlayerToggleSprintEvent(this.player.getBukkitEntity(), action == ServerboundPlayerCommandPacket.Action.START_SPRINTING).callEvent()) {
                ci.cancel();
            }
        }
    }

    // ---- PlayerToggleFlightEvent

    @Inject(method = "handlePlayerAbilities", cancellable = true, require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$flightEvent(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        if (this.player.getAbilities().mayfly && this.player.getAbilities().flying != packet.isFlying()) {
            if (!new PlayerToggleFlightEvent(this.player.getBukkitEntity(), packet.isFlying()).callEvent()) {
                this.player.onUpdateAbilities(); // tell the client its flight state was reverted
                ci.cancel();
            }
        }
    }

    // ---- PlayerItemHeldEvent

    @Inject(method = "handleSetCarriedItem", cancellable = true, require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$itemHeldEvent(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if (this.player.silkard$isImmobile()) {
            ci.cancel();
            return;
        }
        int slot = packet.getSlot();
        if (slot < 0 || slot >= Inventory.getSelectionSize()) {
            return; // vanilla rejects it
        }
        int selected = this.player.getInventory().getSelectedSlot();
        if (slot == selected) {
            ci.cancel(); // Paper - no event without a slot change
            return;
        }
        if (!new PlayerItemHeldEvent(this.player.getBukkitEntity(), selected, slot).callEvent()) {
            this.player.connection.send(new ClientboundSetHeldSlotPacket(selected));
            this.player.resetLastActionTime();
            ci.cancel();
        }
    }

    // ---- PlayerInteractEvent (LEFT_CLICK_AIR), PlayerArmSwingEvent

    @Inject(method = "handlePunch", cancellable = true, require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$immobileCannotSwing(ServerboundPunchPacket packet, CallbackInfo ci) {
        if (this.player.silkard$isImmobile()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePunch", cancellable = true, require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.AFTER))
    private void silkard$armSwingEvents(ServerboundPunchPacket packet, CallbackInfo ci) {
        // SPIGOT-5607: only a swing that hits no block or entity is a left click on air; a bukkit ray
        // trace covers blocks and entities at once. SPIGOT-7429: spectators and non-pickable
        // entities still get the event.
        Location origin = CraftLocation.toBukkit(this.player.getEyePosition(), this.player.level(), this.player.getYRot(), this.player.getXRot());
        double maxRange = Math.max(this.player.blockInteractionRange(), this.player.entityInteractionRange());
        org.bukkit.util.RayTraceResult result = this.player.level().getWorld().rayTrace(origin, origin.getDirection(), maxRange, org.bukkit.FluidCollisionMode.NEVER, false, 0.0, entity -> {
            Entity handle = ((CraftEntity) entity).getHandle();
            return entity != this.player.getBukkitEntity() && this.player.getBukkitEntity().canSee(entity) && !handle.isSpectator() && handle.isPickable() && !handle.isPassengerOfSameVehicle(this.player);
        });
        ItemStack selected = this.player.getInventory().getSelectedItem();
        if (result == null) {
            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, selected, InteractionHand.MAIN_HAND);
        } else {
            GameType gameType = this.player.gameMode.getGameModeForPlayer();
            if (gameType == GameType.ADVENTURE && result.getHitBlock() != null) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, ((CraftBlock) result.getHitBlock()).getPosition(),
                    CraftBlock.blockFaceToNotch(result.getHitBlockFace()), selected, InteractionHand.MAIN_HAND);
            } else if (gameType != GameType.CREATIVE && result.getHitEntity() != null
                && origin.toVector().distanceSquared(result.getHitPosition()) > this.player.entityInteractionRange() * this.player.entityInteractionRange()) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, selected, InteractionHand.MAIN_HAND);
            }
        }

        if (!new io.papermc.paper.event.player.PlayerArmSwingEvent(this.player.getBukkitEntity(), org.bukkit.inventory.EquipmentSlot.HAND).callEvent()) {
            ci.cancel();
        }
    }

    // ---- PlayerSwapHandItemsEvent

    @Inject(method = "handlePlayerAction", cancellable = true, require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$swapHandsEvent(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (this.player.silkard$isImmobile()) {
            ci.cancel();
            return;
        }
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND || !this.hasClientLoaded() || this.player.isSpectator()) {
            return;
        }
        ci.cancel();
        this.player.resetLastActionTime();
        ItemStack swap = this.player.getItemInHand(InteractionHand.OFF_HAND);
        org.bukkit.inventory.ItemStack mainHand = CraftItemStack.asBukkitMirror(swap);
        org.bukkit.inventory.ItemStack offHand = CraftItemStack.asBukkitMirror(this.player.getItemInHand(InteractionHand.MAIN_HAND));
        PlayerSwapHandItemsEvent event = new PlayerSwapHandItemsEvent(this.player.getBukkitEntity(), mainHand.clone(), offHand.clone());
        if (!event.callEvent()) {
            return;
        }
        if (event.getOffHandItem().equals(offHand)) {
            this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
        } else {
            this.player.setItemInHand(InteractionHand.OFF_HAND, CraftItemStack.asNMSCopy(event.getOffHandItem()));
        }
        if (event.getMainHandItem().equals(mainHand)) {
            this.player.setItemInHand(InteractionHand.MAIN_HAND, swap);
        } else {
            this.player.setItemInHand(InteractionHand.MAIN_HAND, CraftItemStack.asNMSCopy(event.getMainHandItem()));
        }
        this.player.stopUsingItem();
    }

    // ---- InventoryCreativeEvent

    @Inject(method = "handleSetCreativeModeSlot", cancellable = true, require = 1, at = @At(value = "INVOKE", target = ENSURE_THREAD, shift = At.Shift.AFTER))
    private void silkard$creativeEvent(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        ci.cancel();
        if (!this.player.hasInfiniteMaterials()) {
            return;
        }
        boolean drop = packet.slotNum() < 0;
        ItemStack itemStack = packet.itemStack();
        if (!itemStack.isItemEnabled(this.player.level().enabledFeatures())) {
            return;
        }

        boolean validSlot = packet.slotNum() >= 1 && packet.slotNum() <= 45;
        boolean validData = itemStack.isEmpty() || itemStack.getCount() <= itemStack.getMaxStackSize();
        if (drop || (validSlot && !ItemStack.matches(this.player.inventoryMenu.getSlot(packet.slotNum()).getItem(), packet.itemStack()))) {
            SlotType type = SlotType.QUICKBAR;
            if (drop) {
                type = SlotType.OUTSIDE;
            } else if (packet.slotNum() < 36) {
                type = packet.slotNum() >= 5 && packet.slotNum() < 9 ? SlotType.ARMOR : SlotType.CONTAINER;
            }
            InventoryCreativeEvent event = new InventoryCreativeEvent(this.player.inventoryMenu.getBukkitView(), type,
                drop ? AbstractContainerMenu.SLOT_CLICKED_OUTSIDE : packet.slotNum(), CraftItemStack.asBukkitCopy(packet.itemStack()));
            event.callEvent();
            itemStack = CraftItemStack.asNMSCopy(event.getCursor());

            switch (event.getResult()) {
                case ALLOW -> validData = true; // the plugin vouches for the item and stack size
                case DEFAULT -> {
                }
                case DENY -> {
                    if (packet.slotNum() >= 0) {
                        this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(),
                            packet.slotNum(), this.player.inventoryMenu.getSlot(packet.slotNum()).getItem()));
                        this.player.connection.send(new ClientboundSetCursorItemPacket(ItemStack.EMPTY.copy()));
                    }
                    return;
                }
            }
        }

        if (validSlot && validData) {
            this.player.inventoryMenu.getSlot(packet.slotNum()).setByPlayer(itemStack);
            this.player.inventoryMenu.setRemoteSlot(packet.slotNum(), itemStack);
            this.player.inventoryMenu.broadcastChanges();
        } else if (drop && validData) {
            if (this.dropSpamThrottler.isUnderThreshold()) {
                this.dropSpamThrottler.increment();
                this.player.drop(itemStack, true, Prediction.PREDICTED);
            } else {
                org.slf4j.LoggerFactory.getLogger(ServerGamePacketListenerImpl.class)
                    .warn("Player {} was dropping items too fast in creative mode, ignoring.", this.player.getPlainTextName());
            }
        }
    }
}
