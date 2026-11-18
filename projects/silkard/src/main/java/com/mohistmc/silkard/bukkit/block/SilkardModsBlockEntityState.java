package com.mohistmc.silkard.bukkit.block;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;

public class SilkardModsBlockEntityState extends CraftBlockEntityState<BlockEntity> {

    public SilkardModsBlockEntityState(World world, BlockEntity blockEntity, boolean useSnapshot) {
        super(world, blockEntity, useSnapshot);
    }

    protected SilkardModsBlockEntityState(SilkardModsBlockEntityState state, Location location) {
        super(state, location);
    }

    @Override
    public SilkardModsBlockEntityState copy() {
        return new SilkardModsBlockEntityState(this, null);
    }

    @Override
    public SilkardModsBlockEntityState copy(Location location) {
        return new SilkardModsBlockEntityState(this, location);
    }
}
