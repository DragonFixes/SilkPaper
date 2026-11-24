package com.mohistmc.silkard.mixin.vanilla.world.entity;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mohistmc.silkard.injected.world.item.component.ContextConsumable;
import com.mohistmc.silkard.injected.world.level.ContextLevel;
import com.mohistmc.silkard.paper.core.PaperEquipmentChange;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import java.util.Map;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item events fired from living entities: PlayerItemConsumeEvent when a player finishes eating or
 * drinking, EntityResurrectEvent on a death a totem could prevent, and PlayerArmorChangeEvent and
 * EntityEquipmentChangedEvent when equipment changes. Also Purpur's totems that work from anywhere
 * in a player's inventory.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityItemEvents {

    @Inject(method = "completeUsingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true, require = 1)
    private void silkard$consumeEvent(CallbackInfo ci, @Share("consumeEvent") LocalRef<PlayerItemConsumeEvent> eventRef) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }
        ItemStack useItem = player.getUseItem();
        InteractionHand hand = player.getUsedItemHand();
        PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player) player.getBukkitEntity(), CraftItemStack.asBukkitCopy(useItem), CraftEquipmentSlot.getHand(hand));
        if (!event.callEvent()) {
            Consumable consumable = useItem.get(DataComponents.CONSUMABLE);
            if (consumable != null) {
                ((ContextConsumable) (Object) consumable).cancelUsingItem(player, useItem);
            }
            player.containerMenu.sendAllDataToRemote();
            player.getBukkitEntity().updateScaledHealth();
            player.stopUsingItem();
            ci.cancel();
            return;
        }
        eventRef.set(event);
    }

    /** Consumes the item a listener swapped in, and hands back the listener's replacement. */
    @WrapOperation(method = "completeUsingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"),
            require = 1)
    private ItemStack silkard$consumeEventItem(ItemStack useItem, Level level, LivingEntity entity, Operation<ItemStack> original,
                                               @Share("consumeEvent") LocalRef<PlayerItemConsumeEvent> eventRef) {
        PlayerItemConsumeEvent event = eventRef.get();
        if (event == null) {
            return original.call(useItem, level, entity);
        }
        ItemStack consumed = CraftItemStack.asBukkitCopy(useItem).equals(event.getItem()) ? useItem : CraftItemStack.asNMSCopy(event.getItem());
        ItemStack result = original.call(consumed, level, entity);
        return event.getReplacement() != null ? CraftItemStack.asNMSCopy(event.getReplacement()) : result;
    }

    /**
     * The whole death-protection check, as CraftBukkit and Paper run it. EntityResurrectEvent fires
     * on every such death, already cancelled when no totem was found; a listener that uncancels it
     * resurrects the entity with the totem of undying's effects.
     */
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true, require = 1)
    private void silkard$resurrectEvent(DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (killingDamage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
            return;
        }

        ItemStack protectionItem = null;
        DeathProtection protection = null;
        InteractionHand usedHand = null;
        ItemStack itemStack = ItemStack.EMPTY;
        for (InteractionHand hand : InteractionHand.values()) {
            itemStack = self.getItemInHand(hand);
            protection = itemStack.get(DataComponents.DEATH_PROTECTION);
            if (protection != null) {
                usedHand = hand;
                protectionItem = itemStack.copy();
                break;
            }
        }

        if (((ContextLevel) self.level()).purpurConfig().totemOfUndyingWorksInInventory && self instanceof ServerPlayer player
                && itemStack.getItem() != Items.TOTEM_OF_UNDYING && player.getBukkitEntity().hasPermission("purpur.inventory_totem")) {
            for (ItemStack item : player.getInventory().getNonEquipmentItems()) {
                if (item.getItem() == Items.TOTEM_OF_UNDYING) {
                    itemStack = item;
                    protectionItem = item.copy();
                    break;
                }
            }
        }

        org.bukkit.inventory.EquipmentSlot handSlot = usedHand != null ? CraftEquipmentSlot.getHand(usedHand) : null;
        EntityResurrectEvent event = new EntityResurrectEvent((org.bukkit.entity.LivingEntity) self.getBukkitEntity(), handSlot);
        event.setCancelled(protectionItem == null);
        event.callEvent();
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }

        if (!itemStack.isEmpty() && protectionItem != null) {
            itemStack.shrink(1);
        }
        if (protection == null) {
            protection = DeathProtection.TOTEM_OF_UNDYING;
        }
        if (protectionItem != null && self instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(protectionItem.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, protectionItem);
            protectionItem.causeUseVibration(self, GameEvent.ITEM_INTERACT_FINISH);
        }

        self.setHealth(1.0F);
        protection.applyEffects(protectionItem, self);
        self.level().broadcastEntityEvent(self, EntityEvent.PROTECTED_FROM_DEATH);
        cir.setReturnValue(true);
    }

    @WrapOperation(method = "collectEquipmentChanges",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;equipmentHasChanged(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"),
            require = 1)
    private boolean silkard$equipmentChangeEvents(LivingEntity self, ItemStack previous, ItemStack current, Operation<Boolean> original,
                                                  @Local EquipmentSlot slot,
                                                  @Share("equipmentChanges") LocalRef<Map<org.bukkit.inventory.EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange>> changesRef) {
        boolean changed = original.call(self, previous, current);
        if (!changed) {
            return false;
        }
        org.bukkit.inventory.ItemStack oldItem = CraftItemStack.asBukkitCopy(previous);
        org.bukkit.inventory.ItemStack newItem = CraftItemStack.asBukkitCopy(current);
        if (self instanceof ServerPlayer player && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            new PlayerArmorChangeEvent((Player) player.getBukkitEntity(), PlayerArmorChangeEvent.SlotType.valueOf(slot.name()), oldItem, newItem).callEvent();
        }
        Map<org.bukkit.inventory.EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> changes = changesRef.get();
        if (changes == null) {
            changes = Maps.newEnumMap(org.bukkit.inventory.EquipmentSlot.class);
            changesRef.set(changes);
        }
        changes.put(CraftEquipmentSlot.getSlot(slot), new PaperEquipmentChange(oldItem, newItem));
        return true;
    }

    @Inject(method = "collectEquipmentChanges", at = @At("RETURN"))
    private void silkard$equipmentChangedEvent(Map<EquipmentSlot, ItemStack> lastEquipmentItems, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir,
                                               @Share("equipmentChanges") LocalRef<Map<org.bukkit.inventory.EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange>> changesRef) {
        Map<org.bukkit.inventory.EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> changes = changesRef.get();
        if (changes != null) {
            new EntityEquipmentChangedEvent((org.bukkit.entity.LivingEntity) ((LivingEntity) (Object) this).getBukkitEntity(), changes).callEvent();
        }
    }
}
