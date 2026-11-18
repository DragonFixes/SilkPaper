package com.mohistmc.silkard.paper.server;

import java.util.Iterator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Block helpers that Paper adds to vanilla block classes as static or extracted methods.
 */
public final class PaperBlocks {

    private PaperBlocks() {
    }

    /**
     * Drops the block's loot and runs its after-break hook, with experience dropping controlled
     * by {@code dropExperience}.
     */
    public static void dropResources(
        final BlockState state, final Level level, final BlockPos pos, final @Nullable BlockEntity blockEntity,
        final @Nullable Entity breaker, final ItemStack tool, final boolean dropExperience
    ) {
        if (level instanceof ServerLevel serverLevel) {
            Block.getDrops(state, serverLevel, pos, blockEntity, breaker, tool).forEach(stack -> Block.popResource(serverLevel, pos, stack));
            state.spawnAfterBreak(serverLevel, pos, tool, dropExperience);
        }
    }

    /**
     * Turns broken ice into water (or nothing where water evaporates), as a player breaking it would.
     */
    public static void iceAfterDestroy(final Level level, final BlockPos pos, final ItemStack destroyedWith) {
        if (!EnchantmentHelper.hasTag(destroyedWith, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
                level.removeBlock(pos, false);
                return;
            }

            final BlockState belowState = level.getBlockState(pos.below());
            if (belowState.is(BlockTags.ICE_MELTS_WHEN_DESTROYED_ABOVE) || belowState.liquid()) {
                level.setBlockAndUpdate(pos, IceBlock.meltsInto());
            }
        }
    }

    /**
     * The sign side that faces the horizontal position {@code (x, z)}, matching how vanilla picks
     * the side a player is looking at.
     */
    public static SignTextSlot getSignSlotFacing(final SignBlockEntity sign, final double x, final double z) {
        if (sign.getBlockState().getBlock() instanceof SignBlock signBlock) {
            final Vec3 signPositionOffset = signBlock.getSignHitboxCenterPosition(sign.getBlockState());
            final double xd = x - (sign.getBlockPos().getX() + signPositionOffset.x);
            final double zd = z - (sign.getBlockPos().getZ() + signPositionOffset.z);
            final float signYRot = signBlock.getYRotationDegrees(sign.getBlockState());
            final float facingYRot = (float) (Mth.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F;
            return Mth.degreesDifferenceAbs(signYRot, facingYRot) <= 90.0F ? SignTextSlot.FRONT : SignTextSlot.BACK;
        }
        return SignTextSlot.FRONT;
    }

    /**
     * {@code config} with a different cooldown length.
     */
    public static TrialSpawner.FullConfig overrideTargetCooldownLength(final TrialSpawner.FullConfig config, final int targetCooldownLength) {
        return new TrialSpawner.FullConfig(config.normal(), config.ominous(), targetCooldownLength, config.requiredPlayerRange());
    }

    /**
     * {@code config} with a different required player range.
     */
    public static TrialSpawner.FullConfig overrideRequiredPlayerRange(final TrialSpawner.FullConfig config, final int requiredPlayerRange) {
        return new TrialSpawner.FullConfig(config.normal(), config.ominous(), config.targetCooldownLength(), requiredPlayerRange);
    }

    /**
     * {@code config} with different normal and ominous spawner configs.
     */
    public static TrialSpawner.FullConfig overrideConfigs(final TrialSpawner.FullConfig config, final Holder<TrialSpawnerConfig> normal, final Holder<TrialSpawnerConfig> ominous) {
        return new TrialSpawner.FullConfig(normal, ominous, config.targetCooldownLength(), config.requiredPlayerRange());
    }

    /**
     * Records {@code playerUUID} as rewarded by the vault, keeping vanilla's limit of 128 entries.
     *
     * @return whether the player was not already recorded
     */
    public static boolean addRewardedPlayer(final VaultServerData serverData, final UUID playerUUID) {
        final boolean added = serverData.rewardedPlayers.add(playerUUID);
        if (serverData.rewardedPlayers.size() > 128) {
            final Iterator<UUID> iterator = serverData.rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        serverData.markChanged();
        return added;
    }

    /**
     * Forgets that the vault rewarded {@code playerUUID}.
     *
     * @return whether the player was recorded
     */
    public static boolean removeRewardedPlayer(final VaultServerData serverData, final UUID playerUUID) {
        final boolean removed = serverData.rewardedPlayers.remove(playerUUID);
        serverData.markChanged();
        return removed;
    }
}