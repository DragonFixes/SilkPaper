package com.mohistmc.silkard.paper.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Level-side helpers that Paper patches into {@code ServerLevel} and {@code BlockGetter},
 * provided as static methods for Paper's CraftWorld.
 */
public final class PaperWorldHooks {

    private PaperWorldHooks() {
    }

    /** Number of entities tracked by the level, including ones not yet accessible. */
    public static int getEntityCount(final ServerLevel level) {
        int count = 0;
        for (final net.minecraft.world.entity.Entity ignored : level.getAllEntities()) {
            count++;
        }
        return count;
    }

    /** Chunks that are loaded at full status. */
    public static List<LevelChunk> getFullChunks(final ServerLevel level) {
        final List<LevelChunk> chunks = new ArrayList<>();
        for (final ChunkHolder holder : level.getChunkSource().chunkMap.visibleChunkMap.values()) {
            final LevelChunk chunk = holder.getFullChunkNow();
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    /** The chunk at any status if it is loaded, without loading or generating it. */
    public static @Nullable ChunkAccess getChunkAtImmediately(final ServerLevel level, final int chunkX, final int chunkZ) {
        final ChunkHolder holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(net.minecraft.world.level.ChunkPos.pack(chunkX, chunkZ));
        return holder == null ? null : holder.getLatestChunk();
    }

    /**
     * Vanilla's lightning target search; with {@code returnNullWhenNoTarget} it answers null
     * instead of the column top when neither a rod nor a living entity is found.
     */
    public static @Nullable BlockPos findLightningTargetAround(final ServerLevel level, final BlockPos pos, final boolean returnNullWhenNoTarget) {
        BlockPos center = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        final Optional<BlockPos> rod = level.findLightningRod(center);
        if (rod.isPresent()) {
            return rod.get();
        }
        final AABB search = AABB.encapsulatingFullBlocks(center, center.atY(level.getMaxY() + 1)).inflate(3.0);
        final List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, search, input -> input.isAlive() && level.canSeeSky(input.blockPosition()));
        if (!entities.isEmpty()) {
            return entities.get(level.getRandom().nextInt(entities.size())).blockPosition();
        }
        if (returnNullWhenNoTarget) {
            return null;
        }
        if (center.getY() == level.getMinY() - 1) {
            center = center.above(2);
        }
        return center;
    }

    /**
     * Vanilla's block clip, skipping blocks the predicate rejects.
     */
    public static BlockHitResult clip(final ServerLevel level, final ClipContext context, final @Nullable Predicate<? super org.bukkit.block.Block> canCollide) {
        if (canCollide == null) {
            return level.clip(context);
        }
        return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (ctx, pos) -> {
            final BlockState state = level.getBlockState(pos);
            if (!canCollide.test(CraftBlock.at(level, pos))) {
                return null;
            }
            final FluidState fluidState = state.getFluidState();
            final Vec3 from = ctx.getFrom();
            final Vec3 to = ctx.getTo();
            final VoxelShape blockShape = ctx.getBlockShape(state, level, pos);
            final BlockHitResult blockHit = level.clipWithInteractionOverride(from, to, pos, blockShape, state);
            final VoxelShape fluidShape = ctx.getFluidShape(fluidState, level, pos);
            final BlockHitResult fluidHit = fluidShape.clip(from, to, pos);
            final double blockDistance = blockHit == null ? Double.MAX_VALUE : from.distanceToSqr(blockHit.getLocation());
            final double fluidDistance = fluidHit == null ? Double.MAX_VALUE : from.distanceToSqr(fluidHit.getLocation());
            return blockDistance <= fluidDistance ? blockHit : fluidHit;
        }, ctx -> {
            final Vec3 delta = ctx.getFrom().subtract(ctx.getTo());
            return BlockHitResult.miss(ctx.getTo(), Direction.getApproximateNearest(delta.x, delta.y, delta.z), BlockPos.containing(ctx.getTo()));
        });
    }

    /**
     * Sends one particle packet to the given players (every player in the level when null),
     * skipping players who cannot see {@code sender}.
     */
    public static int sendParticles(
        final ServerLevel level, final @Nullable List<Player> receivers, final @Nullable ServerPlayer sender,
        final ParticleOptions particle, final boolean overrideLimiter, final boolean alwaysShow,
        final double x, final double y, final double z, final int count,
        final double offsetX, final double offsetY, final double offsetZ,
        final double speedX, final double speedY, final double speedZ,
        final ClientboundLevelParticlesPacket.RandomizationType randomizationType
    ) {
        final ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
            particle, overrideLimiter, alwaysShow, x, y, z,
            (float) offsetX, (float) offsetY, (float) offsetZ,
            (float) speedX, (float) speedY, (float) speedZ,
            count, randomizationType
        );
        final List<ServerPlayer> targets = new ArrayList<>();
        if (receivers == null) {
            targets.addAll(level.players());
        } else {
            for (final Player player : receivers) {
                targets.add(((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle());
            }
        }
        int sent = 0;
        for (final ServerPlayer player : targets) {
            if (player.level() != level) {
                continue;
            }
            if (sender != null && !player.getBukkitEntity().canSee(sender.getBukkitEntity())) {
                continue;
            }
            if (level.sendParticles(player, overrideLimiter, x, y, z, packet)) {
                sent++;
            }
        }
        return sent;
    }

    /** Paper's {@code WorldBorder#applySettings}: moves a live border to the given settings through its setters, so listeners hear every change. */
    public static void applyBorderSettings(final net.minecraft.world.level.border.WorldBorder border, final net.minecraft.world.level.border.WorldBorder.Settings settings) {
        border.setCenter(settings.centerX(), settings.centerZ());
        border.setDamagePerBlock(settings.damagePerBlock());
        border.setSafeZone(settings.safeZone());
        border.setWarningBlocks(settings.warningBlocks());
        border.setWarningTime(settings.warningTime());
        // an in-progress lerp in the settings is not resumed; the border jumps to the start size
        border.setSize(settings.size());
    }
}