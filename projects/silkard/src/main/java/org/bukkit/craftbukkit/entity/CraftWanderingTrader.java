package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.WanderingTrader;

public class CraftWanderingTrader extends CraftAbstractVillager implements WanderingTrader {

    public CraftWanderingTrader(CraftServer server, net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader getHandle() {
        return (net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader) this.entity;
    }

    @Override
    public int getDespawnDelay() {
        return this.getHandle().getDespawnDelay();
    }

    @Override
    public void setDespawnDelay(int despawnDelay) {
        this.getHandle().setDespawnDelay(despawnDelay);
    }

    @Override
    public void setCanDrinkPotion(boolean bool) {
        getHandle().paper$setCanDrinkPotion(bool);
    }

    @Override
    public boolean canDrinkPotion() {
        return getHandle().paper$canDrinkPotion();
    }

    @Override
    public void setCanDrinkMilk(boolean bool) {
        getHandle().paper$setCanDrinkMilk(bool);
    }

    @Override
    public boolean canDrinkMilk() {
        return getHandle().paper$canDrinkMilk();
    }

    @Override
    public org.bukkit.Location getWanderingTowards() {
        net.minecraft.core.BlockPos pos = this.getHandle().getWanderTarget();
        if (pos == null) {
            return null;
        }

        return CraftLocation.toBukkit(pos, this.getHandle().level());
    }

    @Override
    public void setWanderingTowards(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = null;
        if (location != null) {
            pos = CraftLocation.toBlockPos(location);
        }

        this.getHandle().setWanderTarget(pos);
    }
}
