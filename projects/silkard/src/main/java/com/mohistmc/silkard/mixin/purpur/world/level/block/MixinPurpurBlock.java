package com.mohistmc.silkard.mixin.purpur.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Purpur - Ability for hoe to replant crops and nether warts. Breaking a crop with a hoe drops
 * everything except one seed, which is planted back as a fresh crop.
 */
@Mixin(Block.class)
public abstract class MixinPurpurBlock {

    @Inject(method = "playerDestroy", at = @At("HEAD"), cancellable = true)
    private void purpur$hoeReplants(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith, CallbackInfo ci) {
        if (!destroyedWith.is(ItemTags.HOES)) return;
        Object self = this;
        ItemLike seed;
        if (self instanceof CropBlock crop && level.purpurConfig().hoeReplantsCrops) {
            seed = ((PurpurCropBlockAccessor) crop).purpur$getBaseSeedId();
        } else if (self instanceof NetherWartBlock && level.purpurConfig().hoeReplantsNetherWarts) {
            seed = Items.NETHER_WART;
        } else {
            return;
        }
        this.purpur$playerDestroyAndReplant(level, player, pos, state, blockEntity, destroyedWith, seed);
        ci.cancel();
    }

    @Unique
    private void purpur$playerDestroyAndReplant(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack itemInHand, ItemLike itemToReplant) {
        Block self = (Block) (Object) this;
        player.awardStat(Stats.BLOCK_MINED.get(self));
        player.causeFoodExhaustion(0.005F, org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.BLOCK_MINED);
        List<ItemStack> dropList = Block.getDrops(state, level, pos, blockEntity, player, itemInHand);

        boolean planted = false;
        for (ItemStack itemToDrop : dropList) {
            if (!planted && itemToDrop.getItem() == itemToReplant.asItem()) {
                level.setBlock(pos, self.defaultBlockState(), Block.UPDATE_ALL);
                itemToDrop.setCount(itemToDrop.getCount() - 1);
                planted = true;
            }
            Block.popResource(level, pos, itemToDrop);
        }

        state.spawnAfterBreak(level, pos, itemInHand, true);
    }
}
