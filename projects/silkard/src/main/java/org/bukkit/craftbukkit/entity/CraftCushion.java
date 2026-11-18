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
<<<<<<< silkard
    public boolean setFacingDirection(BlockFace face, boolean force) {
        Preconditions.checkArgument(face == BlockFace.UP, "%s is not a valid facing direction", face);

        return force || getHandle().silkard_generation() || getHandle().survives();
    }

    @Override
    public BlockFace getFacing() {
        return BlockFace.UP;
    }

    @Override
    public BlockFace getAttachedFace() {
        return BlockFace.UP;
    }

    @Override
    public void setFacingDirection(BlockFace face) {
=======
    public net.minecraft.world.entity.decoration.Cushion getHandle() {
        return (net.minecraft.world.entity.decoration.Cushion) this.entity;
>>>>>>> paper
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.getByWoolData((byte) this.getHandle().getColor().getId());
    }

    @Override
    public void setColor(final DyeColor color) {
        this.getHandle().setColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }
}
