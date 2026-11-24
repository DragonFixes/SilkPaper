package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Campfire option for lit when placed. */
@Mixin(CampfireBlock.class)
public abstract class MixinPurpurCampfireBlock {

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void purpur$litWhenPlaced(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null && state.getValue(CampfireBlock.LIT) && !context.getLevel().getMinecraftWorld().purpurConfig().campFireLitWhenPlaced) {
            cir.setReturnValue(state.setValue(CampfireBlock.LIT, false));
        }
    }
}
