package com.mohistmc.silkard.mixin.purpur.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The grindstone's result slot. Taking the result fires Paper's BlockExpEvent and Purpur's
 * GrindstoneTakeResultEvent, and the experience dropped is what the events leave. Enchantments
 * the grindstone keeps give no experience.
 */
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public abstract class MixinPurpurGrindstoneResultSlot {

    @Shadow @Final GrindstoneMenu this$0;

    @Unique private @Nullable Player purpur$taker;
    @Unique private ItemStack purpur$taken = ItemStack.EMPTY;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void purpur$rememberTaker(Player player, ItemStack carried, CallbackInfo ci) {
        this.purpur$taker = player;
        this.purpur$taken = carried;
    }

    @ModifyExpressionValue(method = "lambda$onTake$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/GrindstoneMenu$4;getExperienceAmount(Lnet/minecraft/world/level/Level;)I"), require = 1)
    private int purpur$experienceEvents(int amount, Level level, BlockPos pos) {
        // Paper start - Fire BlockExpEvent on grindstone use
        org.bukkit.event.block.BlockExpEvent event = new org.bukkit.event.block.BlockExpEvent(org.bukkit.craftbukkit.block.CraftBlock.at(level, pos), amount);
        event.callEvent();
        amount = event.getExpToDrop();
        // Paper end - Fire BlockExpEvent on grindstone use
        if (this.purpur$taker != null) {
            // Purpur start - Grindstone API
            org.purpurmc.purpur.event.inventory.GrindstoneTakeResultEvent takeEvent = new org.purpurmc.purpur.event.inventory.GrindstoneTakeResultEvent(
                this.purpur$taker.getBukkitEntity(), ((AbstractContainerMenu) this.this$0).getBukkitView(),
                org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitMirror(this.purpur$taken), amount);
            takeEvent.callEvent();
            amount = takeEvent.getExperienceAmount();
            // Purpur end - Grindstone API
        }
        return amount;
    }

    @WrapOperation(method = "getExperienceFromItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z"), require = 1)
    private boolean purpur$keptEnchantments(Holder<Enchantment> enchantment, TagKey<Enchantment> tag, Operation<Boolean> original) {
        return tag == EnchantmentTags.CURSE ? com.mohistmc.silkard.purpur.PurpurHooks.grindstoneKeeps(enchantment) : original.call(enchantment, tag);
    }
}
