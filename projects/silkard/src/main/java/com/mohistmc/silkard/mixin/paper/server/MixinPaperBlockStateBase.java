package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.injected.paper.server.PaperBlockStateBase;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinPaperBlockStateBase implements PaperBlockStateBase {

    @Shadow protected abstract BlockState asState();

    // Built lazily: block states exist before CraftBukkit's block data registry is usable.
    @Unique private @Nullable CraftBlockData silkard$cachedBlockData;
    @Unique private @Nullable Material silkard$cachedMaterial;

    @Override
    public CraftBlockData asBlockData() {
        CraftBlockData data = this.silkard$cachedBlockData;
        if (data == null) {
            data = CraftBlockData.createData(this.asState());
            this.silkard$cachedBlockData = data;
        }
        return (CraftBlockData) data.clone();
    }

    @Override
    public Material getBukkitMaterial() {
        Material material = this.silkard$cachedMaterial;
        if (material == null) {
            material = CraftBlockType.minecraftToBukkit(this.asState().getBlock());
            this.silkard$cachedMaterial = material;
        }
        return material;
    }
}
