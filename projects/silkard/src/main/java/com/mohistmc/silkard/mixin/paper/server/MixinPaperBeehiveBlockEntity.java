package com.mohistmc.silkard.mixin.paper.server;

import com.google.common.collect.Lists;
import com.mohistmc.silkard.injected.paper.server.PaperBeehiveBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinPaperBeehiveBlockEntity extends BlockEntity implements PaperBeehiveBlockEntity {

    @Shadow @Final private List<BeehiveBlockEntity.BeeData> stored;
    @Shadow private @Nullable BlockPos savedFlowerPos;

    @Shadow
    private static boolean releaseOccupant(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity.Occupant occupant, @Nullable List<Entity> spawned, BeehiveBlockEntity.BeeReleaseStatus releaseStatus, @Nullable BlockPos storedFlowerPos) {
        throw new AssertionError();
    }

    private MixinPaperBeehiveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public List<BeehiveBlockEntity.BeeData> getStored() {
        return this.stored;
    }

    @Override
    public void clearBees() {
        this.stored.clear();
    }

    @Override
    public List<Entity> releaseBee(BlockState state, BeehiveBlockEntity.BeeData data, BeehiveBlockEntity.BeeReleaseStatus status, boolean force) {
        // TODO(silkard-paper): honour force; Silkard's force-release switch lives privately in MixinBeehiveBlockEntity,
        //  so a bee can stay inside at night or in rain even when force is requested.
        List<Entity> spawned = Lists.newArrayList();
        releaseOccupant(this.level, this.worldPosition, state, data.occupant, spawned, status, this.savedFlowerPos);
        if (!spawned.isEmpty()) {
            this.stored.remove(data);
            super.setChanged();
        }
        return spawned;
    }
}
