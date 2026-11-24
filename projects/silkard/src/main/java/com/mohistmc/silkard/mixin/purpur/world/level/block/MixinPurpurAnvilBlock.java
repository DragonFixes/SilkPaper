package com.mohistmc.silkard.mixin.purpur.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Purpur - Anvil repair/damage options: iron ingots repair an anvil one stage, obsidian damages it
 * one stage. Either costs the configured amount; 0 turns the option off.
 */
@Mixin(AnvilBlock.class)
public abstract class MixinPurpurAnvilBlock extends FallingBlock {

    private MixinPurpurAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int ingots = level.purpurConfig().anvilRepairIngotsAmount;
        if (ingots > 0 && stack.is(Items.IRON_INGOT)) {
            if (stack.getCount() < ingots || state.is(Blocks.ANVIL)) {
                purpur$sound(level, pos, SoundEvents.ANVIL_HIT); // not enough ingots, or already fully repaired
                return InteractionResult.CONSUME;
            }
            if (state.is(Blocks.DAMAGED_ANVIL)) {
                level.setBlock(pos, Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)), Block.UPDATE_ALL);
            } else if (state.is(Blocks.CHIPPED_ANVIL)) {
                level.setBlock(pos, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)), Block.UPDATE_ALL);
            }
            if (!player.getAbilities().instabuild) stack.shrink(ingots);
            purpur$sound(level, pos, SoundEvents.ANVIL_PLACE);
            return InteractionResult.CONSUME;
        }
        int obsidian = level.purpurConfig().anvilDamageObsidianAmount;
        if (obsidian > 0 && stack.is(Items.OBSIDIAN)) {
            if (stack.getCount() < obsidian) {
                purpur$sound(level, pos, SoundEvents.ANVIL_HIT);
                return InteractionResult.CONSUME;
            }
            if (state.is(Blocks.DAMAGED_ANVIL)) {
                level.destroyBlock(pos, false);
            } else if (state.is(Blocks.CHIPPED_ANVIL)) {
                level.setBlock(pos, Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)), Block.UPDATE_ALL);
            } else if (state.is(Blocks.ANVIL)) {
                level.setBlock(pos, Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)), Block.UPDATE_ALL);
            }
            if (!player.getAbilities().instabuild) stack.shrink(obsidian);
            purpur$sound(level, pos, SoundEvents.ANVIL_LAND);
            return InteractionResult.CONSUME;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Unique
    private static void purpur$sound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
