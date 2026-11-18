package org.bukkit.craftbukkit.entity;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cushion;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CraftCushion extends CraftBlockAttachedEntity implements Cushion {

    public CraftCushion(final CraftServer server, final net.minecraft.world.entity.decoration.Cushion entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.decoration.Cushion getHandle() {
        return (net.minecraft.world.entity.decoration.Cushion) this.entity;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.getByWoolData((byte) this.getHandle().getColor().getId());
    }

    @Override
    public void setColor(final DyeColor color) {
        this.getHandle().setColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public boolean setFacingDirection(final org.bukkit.block.BlockFace face, final boolean force) {
        com.google.common.base.Preconditions.checkArgument(face == org.bukkit.block.BlockFace.UP, "%s is not a valid facing direction", face);
        return force || this.getHandle().silkard_generation() || this.getHandle().survives();
    }

    @Override
    public void setFacingDirection(final org.bukkit.block.BlockFace face) {
        this.setFacingDirection(face, false);
    }

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return org.bukkit.block.BlockFace.UP;
    }

    @Override
    public org.bukkit.block.BlockFace getAttachedFace() {
        return org.bukkit.block.BlockFace.DOWN;
    }
}
