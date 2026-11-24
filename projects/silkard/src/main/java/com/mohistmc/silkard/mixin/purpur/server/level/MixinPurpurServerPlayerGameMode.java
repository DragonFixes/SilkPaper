package com.mohistmc.silkard.mixin.purpur.server.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Sneak to bulk process composter. Sneaking with an item normally skips the clicked
 * block's own use; for a composter with bulk processing on, the block is used anyway, and
 * {@code MixinPurpurComposterBlock} then composts the whole stack.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinPurpurServerPlayerGameMode {

    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"), require = 1)
    private boolean purpur$bulkComposter(boolean sneaking, ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult) {
        return sneaking && !(level.purpurConfig().composterBulkProcess && level.getBlockState(hitResult.getBlockPos()).is(Blocks.COMPOSTER));
    }
}
