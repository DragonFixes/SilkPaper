package com.mohistmc.silkard.mixin.purpur.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Tilt;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Purpur - Big dripleaf tilt delay: per-tilt ticks before the leaf moves on, -1 to stay put. */
@Mixin(BigDripleafBlock.class)
public abstract class MixinPurpurBigDripleafBlock {

    @Redirect(method = "setTiltAndScheduleTick", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"))
    private int purpur$tiltDelay(Object2IntMap<Tilt> delays, Object tilt, BlockState state, Level level, BlockPos pos, Tilt tilt2, @Nullable SoundEvent sound) {
        return level.purpurConfig().bigDripleafTiltDelay.getOrDefault(tilt2, -1);
    }
}
