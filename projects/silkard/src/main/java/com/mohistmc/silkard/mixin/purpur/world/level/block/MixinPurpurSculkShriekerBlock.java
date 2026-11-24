package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Config for sculk shrieker can_summon state. */
@Mixin(SculkShriekerBlock.class)
public abstract class MixinPurpurSculkShriekerBlock {

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void purpur$canSummonDefault(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null) {
            cir.setReturnValue(state.setValue(SculkShriekerBlock.CAN_SUMMON, context.getLevel().purpurConfig().sculkShriekerCanSummonDefault));
        }
    }
}
