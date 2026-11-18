package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Trident;

public class CraftTrident extends CraftAbstractArrow implements Trident {

    public CraftTrident(CraftServer server, ThrownTrident entity) {
        super(server, entity);
    }

    @Override
    public ThrownTrident getHandle() {
        return (ThrownTrident) this.entity;
    }

    @Override
    public boolean hasGlint() {
        return this.getHandle().isFoil();
    }

    @Override
    public void setGlint(boolean glint) {
        this.getHandle().getEntityData().set(net.minecraft.world.entity.projectile.arrow.ThrownTrident.ID_FOIL, glint);
    }

    @Override
    public int getLoyaltyLevel() {
        return this.getHandle().getEntityData().get(net.minecraft.world.entity.projectile.arrow.ThrownTrident.ID_LOYALTY);
    }

    @Override
    public void setLoyaltyLevel(int loyaltyLevel) {
        com.google.common.base.Preconditions.checkArgument(loyaltyLevel >= 0 && loyaltyLevel <= 127, "The loyalty level has to be between 0 and 127");
        this.getHandle().getEntityData().set(net.minecraft.world.entity.projectile.arrow.ThrownTrident.ID_LOYALTY, (byte) loyaltyLevel);
    }

    @Override
    public boolean hasDealtDamage() {
        return this.getHandle().dealtDamage;
    }

    @Override
    public void setHasDealtDamage(boolean hasDealtDamage) {
        this.getHandle().dealtDamage = hasDealtDamage;
    }
}
