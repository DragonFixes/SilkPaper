package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Purpur - sneak to bulk process composter. Each further item goes through {@code useItemOn}
 * again, so every item fires the same compost events as a single click. It stops once the stack
 * is empty or a use changes neither the stack nor the fill level.
 */
@Mixin(ComposterBlock.class)
public abstract class MixinPurpurComposterBlock {

    @Unique private static final ThreadLocal<Boolean> purpur$bulk = ThreadLocal.withInitial(() -> false);

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void purpur$bulkProcess(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult,
                                    CallbackInfoReturnable<InteractionResult> cir) {
        if (purpur$bulk.get() || cir.getReturnValue() != InteractionResult.SUCCESS || level.isClientSide()
            || !level.purpurConfig().composterBulkProcess || !player.isShiftKeyDown()) {
            return;
        }
        purpur$bulk.set(true);
        try {
            ComposterBlock self = (ComposterBlock) (Object) this;
            while (itemStack.getCount() > 0) {
                BlockState current = level.getBlockState(pos);
                if (!current.is(self)) break;
                int oldCount = itemStack.getCount();
                int oldLevel = current.getValue(ComposterBlock.LEVEL);
                InteractionResult result = current.useItemOn(itemStack, level, player, hand, hitResult);
                BlockState next = level.getBlockState(pos);
                int newLevel = next.is(self) ? next.getValue(ComposterBlock.LEVEL) : oldLevel;
                if (result != InteractionResult.SUCCESS || (itemStack.getCount() == oldCount && newLevel == oldLevel)) break;
            }
        } finally {
            purpur$bulk.set(false);
        }
    }
}
