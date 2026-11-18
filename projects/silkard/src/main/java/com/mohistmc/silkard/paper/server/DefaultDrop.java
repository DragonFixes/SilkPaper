package com.mohistmc.silkard.paper.server;

import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.jspecify.annotations.Nullable;

/**
 * Paper's {@code Entity.DefaultDrop}: an item a dying entity would drop, with the vanilla code
 * that drops it. Plugins see {@link #stack()} in the death event; if they keep the item type, the
 * vanilla consumer drops it, otherwise the event's fallback does.
 */
public record DefaultDrop(Item item, org.bukkit.inventory.ItemStack stack, @Nullable Consumer<ItemStack> dropConsumer) {

    public DefaultDrop(final ItemStack stack, final @Nullable Consumer<ItemStack> dropConsumer) {
        this(stack.getItem(), CraftItemStack.asCraftMirror(stack), dropConsumer);
    }

    public void runConsumer(final Consumer<org.bukkit.inventory.ItemStack> fallback) {
        if (this.dropConsumer == null || CraftItemType.bukkitToMinecraft(this.stack.getType()) != this.item) {
            fallback.accept(this.stack);
        } else {
            this.dropConsumer.accept(CraftItemStack.asNMSCopy(this.stack));
        }
    }
}
