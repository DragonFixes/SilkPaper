package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Option for chests to open even with a solid block on top. */
@Mixin(ChestBlock.class)
public abstract class MixinPurpurChestBlock {

    @Inject(method = "isBlockedChestByBlock", at = @At("HEAD"), cancellable = true)
    private static void purpur$openWithBlockOnTop(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof Level world && world.purpurConfig().chestOpenWithBlockOnTop) {
            cir.setReturnValue(false);
        }
    }
}
