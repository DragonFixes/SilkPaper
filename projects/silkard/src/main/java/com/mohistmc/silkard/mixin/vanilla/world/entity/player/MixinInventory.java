package com.mohistmc.silkard.mixin.vanilla.world.entity.player;

import com.mohistmc.silkard.injected.world.entity.player.ContextInventory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Mgazul
 * @date 2026/6/8
 */
@Mixin(Inventory.class)
public abstract class MixinInventory implements ContextInventory {

    // @formatter:off
    @Shadow public Player player;
    @Shadow public NonNullList<ItemStack> items;
    @Shadow @Final public EntityEquipment equipment;
    // @formatter:on

    // Equipment slots ordered by their inventory slot index (armor, then offhand, body, saddle).
    // `items` only holds the 36 storage slots; everything after them lives in `equipment`.
    @Unique
    private static final EquipmentSlot[] silkard$EQUIPMENT_SLOTS_SORTED_BY_INDEX = Inventory.EQUIPMENT_SLOT_MAPPING.int2ObjectEntrySet()
        .stream()
        .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
        .map(Map.Entry::getValue)
        .toArray(EquipmentSlot[]::new);

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<>();
    private int maxStack = Inventory.MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> combined = new ArrayList<>(this.items.size() + silkard$EQUIPMENT_SLOTS_SORTED_BY_INDEX.length);
        combined.addAll(this.items);
        for (EquipmentSlot slot : silkard$EQUIPMENT_SLOTS_SORTED_BY_INDEX) {
            combined.add(this.equipment.get(slot)); // empty stacks included so indexes match inventory slots
        }
        return combined;
    }

    @Override
    public List<ItemStack> getArmorContents() {
        List<ItemStack> armor = new ArrayList<>(4);
        for (EquipmentSlot slot : silkard$EQUIPMENT_SLOTS_SORTED_BY_INDEX) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                armor.add(this.equipment.get(slot));
            }
        }
        return armor;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public org.bukkit.inventory.InventoryHolder getOwner() {
        return this.player.getBukkitEntity();
    }

    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    public Location getLocation() {
        return player.getBukkitEntity().getLocation();
    }

    // Watch method above! :D
    @Override
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.items.get(i);
            if (itemstack1.isEmpty()) return itemstack.getCount();

            if (hasRemainingSpaceForItem(itemstack1, itemstack)) {
                remains -= (itemstack1.getMaxStackSize() < this.getMaxStackSize() ? itemstack1.getMaxStackSize() : this.getMaxStackSize()) - itemstack1.getCount();
            }
            if (remains <= 0) return itemstack.getCount();
        }
        ItemStack offhandItemStack = this.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        if (hasRemainingSpaceForItem(offhandItemStack, itemstack)) {
            remains -= (offhandItemStack.getMaxStackSize() < this.getMaxStackSize() ? offhandItemStack.getMaxStackSize() : this.getMaxStackSize()) - offhandItemStack.getCount();
        }
        if (remains <= 0) return itemstack.getCount();

        return itemstack.getCount() - remains;
    }

    @Shadow
    private boolean hasRemainingSpaceForItem(ItemStack slotItemStack, ItemStack newItemStack) { return false; }
    // CraftBukkit end
}
