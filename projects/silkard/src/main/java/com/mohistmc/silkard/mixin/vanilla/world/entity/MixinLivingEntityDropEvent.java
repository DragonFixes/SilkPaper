package com.mohistmc.silkard.mixin.vanilla.world.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CraftBukkit - fire PlayerDropItemEvent. A player dropping an item fires the event before the
 * item entity is added; a cancelled drop puts the item back in the hand or the inventory.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityDropEvent {

    @Shadow public abstract @Nullable ItemEntity createItemStackToDrop(ItemStack itemStack, boolean randomly, boolean thrownFromHand);

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;", cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void silkard$dropEvent(ItemStack itemStack, boolean thrownFromHand, Prediction prediction, CallbackInfoReturnable<ItemEntity> cir, @Local ItemEntity entity) {
        if (silkard$dropCancelled(entity, thrownFromHand)) {
            cir.setReturnValue(null);
        }
    }

    /**
     * CraftBukkit's drop with an explicit event switch, as {@code Inventory#dropAll} and other
     * drops that must not fire the event use it.
     */
    public @Nullable ItemEntity drop(ItemStack itemStack, boolean randomly, boolean thrownFromHand, boolean callEvent) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (itemStack.isEmpty() || self.level().isClientSide()) {
            return null;
        }
        ItemEntity entity = this.createItemStackToDrop(itemStack, randomly, thrownFromHand);
        if (entity == null || (callEvent && silkard$dropCancelled(entity, thrownFromHand))) {
            return null;
        }
        self.level().addFreshEntity(entity);
        return entity;
    }

    /** Fires PlayerDropItemEvent for a player's drop; on cancel, returns the item and reports true. */
    @Unique
    private boolean silkard$dropCancelled(ItemEntity entity, boolean thrownFromHand) {
        if (!(((LivingEntity) (Object) this).getBukkitEntity() instanceof org.bukkit.entity.Player player)) {
            return false;
        }
        org.bukkit.entity.Item drop = (org.bukkit.entity.Item) entity.getBukkitEntity();
        org.bukkit.event.player.PlayerDropItemEvent event = new org.bukkit.event.player.PlayerDropItemEvent(player, drop);
        event.callEvent();
        if (!event.isCancelled()) {
            return false;
        }
        org.bukkit.inventory.ItemStack inHand = player.getInventory().getItemInMainHand();
        if (thrownFromHand && inHand.getAmount() == 0) {
            player.getInventory().setItemInMainHand(drop.getItemStack()); // the whole stack was dropped
        } else if (thrownFromHand && inHand.isSimilar(drop.getItemStack()) && inHand.getAmount() < inHand.getMaxStackSize() && drop.getItemStack().getAmount() == 1) {
            inHand.setAmount(inHand.getAmount() + 1); // one item was dropped
            player.getInventory().setItemInMainHand(inHand);
        } else {
            player.getInventory().addItem(drop.getItemStack());
        }
        return true;
    }
}
