package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.entity.Piglin;
import org.bukkit.inventory.Inventory;

public class CraftPiglin extends CraftPiglinAbstract implements Piglin, com.destroystokyo.paper.entity.CraftRangedEntity<net.minecraft.world.entity.monster.piglin.Piglin> { // Paper

    public CraftPiglin(CraftServer server, net.minecraft.world.entity.monster.piglin.Piglin entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.piglin.Piglin getHandle() {
        return (net.minecraft.world.entity.monster.piglin.Piglin) this.entity;
    }

    @Override
    public boolean isAbleToHunt() {
        return !this.getHandle().cannotHunt;
    }

    @Override
    public void setIsAbleToHunt(boolean flag) {
        this.getHandle().cannotHunt = !flag;
    }

    @Override
    public boolean addBarterMaterial(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftItemType.bukkitToMinecraft(material);
<<<<<<< silkard
        return getHandle().silkard$allowedBarterItems().add(item);
=======
        return this.getHandle().allowedBarterItems.add(item);
>>>>>>> paper
    }

    @Override
    public boolean removeBarterMaterial(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftItemType.bukkitToMinecraft(material);
<<<<<<< silkard
        return getHandle().silkard$allowedBarterItems().remove(item);
=======
        return this.getHandle().allowedBarterItems.remove(item);
>>>>>>> paper
    }

    @Override
    public boolean addMaterialOfInterest(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftItemType.bukkitToMinecraft(material);
<<<<<<< silkard
        return getHandle().silkard$interestItems().add(item);
=======
        return this.getHandle().interestItems.add(item);
>>>>>>> paper
    }

    @Override
    public boolean removeMaterialOfInterest(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftItemType.bukkitToMinecraft(material);
<<<<<<< silkard
        return getHandle().silkard$interestItems().remove(item);
=======
        return this.getHandle().interestItems.remove(item);
>>>>>>> paper
    }

    @Override
    public Set<Material> getInterestList() {
<<<<<<< silkard
        return Collections.unmodifiableSet(getHandle().silkard$interestItems().stream().map(CraftItemType::minecraftToBukkit).collect(Collectors.toSet()));
=======
        return Collections.unmodifiableSet(this.getHandle().interestItems.stream().map(CraftItemType::minecraftToBukkit).collect(Collectors.toSet()));
>>>>>>> paper
    }

    @Override
    public Set<Material> getBarterList() {
<<<<<<< silkard
        return Collections.unmodifiableSet(getHandle().silkard$allowedBarterItems().stream().map(CraftItemType::minecraftToBukkit).collect(Collectors.toSet()));
=======
        return Collections.unmodifiableSet(this.getHandle().allowedBarterItems.stream().map(CraftItemType::minecraftToBukkit).collect(Collectors.toSet()));
>>>>>>> paper
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(this.getHandle().getInventory());
    }

    @Override
    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.getHandle().setChargingCrossbow(chargingCrossbow);
    }

    @Override
    public boolean isChargingCrossbow() {
        return this.getHandle().isChargingCrossbow();
    }

    @Override
    public void setDancing(boolean dancing) {
        if (dancing) {
            this.getHandle().getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING, true);
            this.getHandle().getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION, this.getHandle().getOnPos());
        } else {
            this.getHandle().getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING);
            this.getHandle().getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION);
        }
    }

    @Override
    public void setDancing(long duration) {
        this.getHandle().getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING, true, duration);
        this.getHandle().getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION, this.getHandle().getOnPos(), duration);
    }

    @Override
    public boolean isDancing() {
        return this.getHandle().isDancing();
    }
}
