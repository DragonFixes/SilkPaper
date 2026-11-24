package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Configurable damage settings for magma blocks. */
@Mixin(MagmaBlock.class)
public abstract class MixinPurpurMagmaBlock {

    @ModifyExpressionValue(method = "stepOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSteppingCarefully()Z"))
    private boolean purpur$damageWhenSneaking(boolean steppingCarefully, Level level, BlockPos pos, BlockState onState, Entity entity) {
        return steppingCarefully && !level.purpurConfig().magmaBlockDamageWhenSneaking;
    }
}
