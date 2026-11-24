package com.mohistmc.silkard.mixin.vanilla.server.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.silkard.paper.core.PaperEntities;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit/Paper entity interaction events.
 *
 * <p>Right-clicking an entity fires PlayerInteractAtEntityEvent, which shares its handler list
 * with PlayerInteractEntityEvent, so listeners of either see it. A cancelled event skips
 * {@code interactOn} and resends whatever the client predicted (held item, leash, entity data,
 * equipment). Clicking or attacking an entity id the server does not know fires
 * PlayerUseUnknownEntityEvent, which packet-based NPC plugins rely on.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerInteractEvents {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleInteract", cancellable = true, require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    private void silkard$immobileCannotInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player.silkard$isImmobile()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void silkard$useUnknownEntity(ServerboundInteractPacket packet, CallbackInfo ci, @Local Entity target) {
        if (target == null) {
            CraftEventFactory.callPlayerUseUnknownEntityEvent(this.player, packet.entityId(), false, packet.hand(), packet.location());
        }
    }

    @Inject(method = "handleAttack", require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void silkard$attackUnknownEntity(ServerboundAttackPacket packet, CallbackInfo ci, @Local Entity target) {
        if (target == null) {
            CraftEventFactory.callPlayerUseUnknownEntityEvent(this.player, packet.entityId(), true, InteractionHand.MAIN_HAND, null);
        }
    }

    @WrapOperation(method = "handleInteract", require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult silkard$interactEntityEvent(ServerPlayer player, Entity target, InteractionHand hand, Vec3 location, Operation<InteractionResult> original) {
        ItemStack tool = player.getItemInHand(hand);
        Item itemType = tool.getItem();
        boolean triggerLeashUpdate = tool.is(Items.LEAD) && target instanceof Leashable;

        PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(
            player.getBukkitEntity(), target.getBukkitEntity(), CraftVector.toBukkit(location), CraftEquipmentSlot.getHand(hand));
        event.callEvent();
        boolean resendData = event.isCancelled() || !player.getItemInHand(hand).is(itemType);

        if (resendData) {
            // SPIGOT-4048, SPIGOT-6859: the client already removed a bucketed mob
            if (itemType == Items.WATER_BUCKET && target instanceof net.minecraft.world.entity.Bucketable && target instanceof LivingEntity) {
                PaperEntities.resendPossiblyDesyncedEntityData(target, player);
            }
            if (triggerLeashUpdate) {
                player.connection.send(new ClientboundSetEntityLinkPacket(target, ((Leashable) target).getLeashHolder()));
            }
            target.refreshEntityData(player);
            // SPIGOT-7136 - Allays; Paper - horse armor
            if (target instanceof net.minecraft.world.entity.animal.allay.Allay || target instanceof net.minecraft.world.entity.animal.equine.AbstractHorse) {
                LivingEntity living = (LivingEntity) target;
                player.connection.send(new ClientboundSetEquipmentPacket(target.getId(),
                    java.util.Arrays.stream(EquipmentSlot.values())
                        .map(slot -> com.mojang.datafixers.util.Pair.of(slot, living.getItemBySlot(slot).copy()))
                        .toList()));
            }
            player.containerMenu.sendAllDataToRemote();
        }

        if (event.isCancelled()) {
            return InteractionResult.PASS;
        }
        return original.call(player, target, hand, location);
    }
}
