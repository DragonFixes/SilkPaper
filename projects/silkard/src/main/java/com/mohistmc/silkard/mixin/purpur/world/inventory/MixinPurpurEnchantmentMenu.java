package com.mohistmc.silkard.mixin.purpur.world.inventory;

import com.mohistmc.silkard.purpur.PurpurLapisHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Purpur - Enchantment Table Persists Lapis. Opening a table fills the lapis slot from the table;
 * closing it stores the lapis back in the table instead of returning it to the player.
 */
@Mixin(EnchantmentMenu.class)
public abstract class MixinPurpurEnchantmentMenu extends AbstractContainerMenu {

    @Shadow @Final private ContainerLevelAccess access;

    private MixinPurpurEnchantmentMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void purpur$loadLapis(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        access.execute((level, pos) -> {
            if (level.purpurConfig().enchantmentTableLapisPersists && level.getBlockEntity(pos) instanceof PurpurLapisHolder table) {
                this.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, table.purpur$getLapis()));
            }
        });
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void purpur$storeLapis(Player player, CallbackInfo ci) {
        this.access.execute((level, pos) -> {
            if (!level.purpurConfig().enchantmentTableLapisPersists) return;
            if (level.getBlockEntity(pos) instanceof PurpurLapisHolder table) {
                table.purpur$setLapis(this.getSlot(1).getItem().getCount());
            }
            this.getSlot(1).set(ItemStack.EMPTY);
        });
    }
}
