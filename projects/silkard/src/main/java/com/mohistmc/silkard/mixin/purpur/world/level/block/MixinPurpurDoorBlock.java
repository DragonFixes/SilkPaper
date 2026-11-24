package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - Option to make doors require redstone. */
@Mixin(DoorBlock.class)
public abstract class MixinPurpurDoorBlock {

    @Shadow public abstract net.minecraft.world.level.block.state.properties.BlockSetType type();

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void purpur$requiresRedstone(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.type().canOpenByHand() && purpur$requiresRedstone(level, state, pos)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

    /** Whether hand use is denied; also resends both halves so the client undoes its prediction. */
    @Unique
    private static boolean purpur$requiresRedstone(Level level, BlockState state, BlockPos pos) {
        if (level.purpurConfig().doorRequiresRedstone.contains(state.getBlock())) {
            BlockPos otherPos = pos.relative(state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
            BlockState otherState = level.getBlockState(otherPos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            level.sendBlockUpdated(otherPos, otherState, otherState, Block.UPDATE_ALL);
            return true;
        }
        return false;
    }
}
