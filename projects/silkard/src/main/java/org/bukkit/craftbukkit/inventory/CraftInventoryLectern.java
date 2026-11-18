package org.bukkit.craftbukkit.inventory;

import com.mohistmc.silkard.bukkit.inventory.BukkitLecternInventory;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.LecternInventory;

public class CraftInventoryLectern extends CraftInventory implements LecternInventory {

    public MenuProvider provider;

    public CraftInventoryLectern(Container inventory) {
        super(inventory);
<<<<<<< silkard
        if (inventory instanceof BukkitLecternInventory bukkitLecternInventory) {
            this.tile = bukkitLecternInventory.getLectern();
=======
        if (inventory instanceof LecternBlockEntity.LecternInventory) {
            this.provider = ((LecternBlockEntity.LecternInventory) inventory).getLectern();
>>>>>>> paper
        }
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) this.inventory.getOwner();
    }
}
