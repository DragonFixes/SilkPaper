package com.mohistmc.silkard.paper.core;

import com.mohistmc.silkard.injected.world.entity.ContextEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Static counterparts of entity helpers that Paper and Purpur add to vanilla's {@link Entity}.
 */
public final class PaperEntities {

    private PaperEntities() {
    }

    /** The bounding box the entity would have at the given position in its current pose. */
    public static AABB getBoundingBoxAt(Entity entity, double x, double y, double z) {
        return entity.getDimensions(entity.getPose()).makeBoundingBox(x, y, z);
    }

    /**
     * Resends an entity's pairing data to one player, for data the client may have predicted
     * differently from the server.
     */
    public static void resendPossiblyDesyncedEntityData(Entity entity, ServerPlayer player) {
        if (!player.getBukkitEntity().canSee(((ContextEntity) entity).getBukkitEntity())) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        ChunkMap.TrackedEntity tracker = level.getChunkSource().chunkMap.entityMap.get(entity.getId());
        if (tracker == null) {
            return;
        }
        final List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
        tracker.serverEntity.sendPairingData(player, list::add);
        player.connection.send(new ClientboundBundlePacket(list));
    }

    /**
     * The entity's type id. With {@code includeNonSaveable}, types that vanilla never saves
     * (players, fishing bobbers) still return their id.
     */
    public static String getEncodeId(Entity entity, boolean includeNonSaveable) {
        if (!includeNonSaveable) {
            return entity.getEncodeId();
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    /**
     * Writes the entity with its id and passengers, including entities vanilla would refuse to
     * save (passengers, non-serializable types). Returns {@code false} only for removed entities.
     */
    public static boolean saveRaw(Entity entity, ValueOutput output, boolean includeAll) {
        if (entity.isRemoved()) {
            return false;
        }
        output.putString(Entity.TAG_ID, getEncodeId(entity, true));
        ((ContextEntity) entity).saveWithoutId(output, includeAll);
        return true;
    }

    /** Whether the sun would set this entity on fire this tick (Purpur daylight burn API). */
    public static boolean isSunBurnTick(Entity entity) {
        if (entity instanceof Mob mob) {
            return mob.isSunBurnTick();
        }
        if (entity.level().isClientSide() || !entity.level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, entity.position())) {
            return false;
        }
        float brightness = entity.getLightLevelDependentMagicValue();
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        boolean shielded = entity.isInWaterOrRain() || entity.isInPowderSnow || entity.wasInPowderSnow;
        return brightness > 0.5F && entity.getRandom().nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && !shielded && entity.level().canSeeSky(eyePos);
    }

    /** Block hit along the entity's look direction, up to {@code maxDistance} (1-120) blocks. */
    public static net.minecraft.world.phys.HitResult getRayTrace(net.minecraft.world.entity.LivingEntity entity, int maxDistance, net.minecraft.world.level.ClipContext.Fluid fluid) {
        if (maxDistance < 1 || maxDistance > 120) {
            throw new IllegalArgumentException("maxDistance must be between 1-120");
        }
        net.minecraft.world.phys.Vec3 start = entity.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 end = start.add(entity.getLookAngle().scale(maxDistance));
        return entity.level().clip(new net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, fluid, entity));
    }

    /** Closest pickable, non-spectator entity along the look direction, up to {@code maxDistance} (1-120) blocks. */
    public static net.minecraft.world.phys.@org.jspecify.annotations.Nullable EntityHitResult getTargetEntity(net.minecraft.world.entity.LivingEntity entity, int maxDistance) {
        if (maxDistance < 1 || maxDistance > 120) {
            throw new IllegalArgumentException("maxDistance must be between 1-120");
        }
        net.minecraft.world.phys.Vec3 start = entity.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 direction = entity.getLookAngle();
        net.minecraft.world.phys.Vec3 end = start.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
        List<Entity> candidates = entity.level().getEntities(entity,
            entity.getBoundingBox().expandTowards(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance).inflate(1.0),
            net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.and(Entity::isPickable));
        double closest = 0.0;
        net.minecraft.world.phys.EntityHitResult result = null;
        for (Entity candidate : candidates) {
            double inflation = candidate.getPickRadius();
            java.util.Optional<net.minecraft.world.phys.Vec3> hit = candidate.getBoundingBox().inflate(inflation).clip(start, end);
            if (hit.isPresent()) {
                double distance = start.distanceToSqr(hit.get());
                if (distance < closest || closest == 0.0) {
                    result = new net.minecraft.world.phys.EntityHitResult(candidate, hit.get());
                    closest = distance;
                }
            }
        }
        return result;
    }

    /** Whether the straight line from {@code start} to {@code end} hits no block collision shape. */
    public static net.minecraft.world.phys.HitResult.Type clipDirect(net.minecraft.world.level.Level level, net.minecraft.world.phys.Vec3 start, net.minecraft.world.phys.Vec3 end, Entity entity) {
        return level.clip(new net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, entity)).getType();
    }

    /** An experience orb carrying Paper's origin data, which vanilla's constructors cannot take. */
    public static net.minecraft.world.entity.ExperienceOrb experienceOrb(net.minecraft.world.level.Level level, double x, double y, double z, int value,
                                                                     org.bukkit.entity.ExperienceOrb.SpawnReason reason,
                                                                     @org.jspecify.annotations.Nullable Entity triggerEntity,
                                                                     @org.jspecify.annotations.Nullable Entity sourceEntity) {
        net.minecraft.world.entity.ExperienceOrb orb = new net.minecraft.world.entity.ExperienceOrb(level, x, y, z, value);
        orb.paper$setOrbSpawnReason(reason);
        orb.paper$setTriggerEntityId(triggerEntity == null ? null : triggerEntity.getUUID());
        orb.paper$setSourceEntityId(sourceEntity == null ? null : sourceEntity.getUUID());
        return orb;
    }

    /**
     * Makes the witch start drinking {@code potion} the way its own AI does: the potion goes to the
     * main hand, the drink timer starts, the sound plays and the drinking slowdown applies. An empty
     * stack stops drinking.
     */
    public static void setWitchDrinkingPotion(net.minecraft.world.entity.monster.Witch witch, net.minecraft.world.item.ItemStack potion) {
        witch.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, potion);
        net.minecraft.world.entity.ai.attributes.AttributeInstance speed = witch.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (potion.isEmpty()) {
            witch.setUsingItem(false);
            witch.usingTime = 0;
            if (speed != null) {
                speed.removeModifier(net.minecraft.world.entity.monster.Witch.SPEED_MODIFIER_DRINKING_ID);
            }
            return;
        }
        witch.usingTime = witch.getMainHandItem().getUseDuration(witch);
        witch.setUsingItem(true);
        if (!witch.isSilent()) {
            witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(), net.minecraft.sounds.SoundEvents.WITCH_DRINK, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }
        if (speed != null) {
            speed.removeModifier(net.minecraft.world.entity.monster.Witch.SPEED_MODIFIER_DRINKING_ID);
            speed.addTransientModifier(net.minecraft.world.entity.monster.Witch.SPEED_MODIFIER_DRINKING);
        }
    }
}