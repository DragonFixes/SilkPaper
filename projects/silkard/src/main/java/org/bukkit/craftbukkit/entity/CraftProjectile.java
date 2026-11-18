package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Projectile;

public abstract class CraftProjectile extends AbstractProjectile implements Projectile {

    public CraftProjectile(CraftServer server, net.minecraft.world.entity.projectile.Projectile entity) {
        super(server, entity);
    }

    @Override
<<<<<<< silkard
    public ProjectileSource getShooter() {
        return getHandle().silkard_projectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftEntity) {
            getHandle().setOwner(((CraftEntity) shooter).entity);
        } else {
            getHandle().setOwner(null);
        }
        getHandle().silkard_projectileSource(shooter);
    }

    @Override
    public boolean doesBounce() {
        return false;
    }

    @Override
    public void setBounce(boolean doesBounce) {}

    @Override
=======
>>>>>>> paper
    public net.minecraft.world.entity.projectile.Projectile getHandle() {
        return (net.minecraft.world.entity.projectile.Projectile) this.entity;
    }
}
