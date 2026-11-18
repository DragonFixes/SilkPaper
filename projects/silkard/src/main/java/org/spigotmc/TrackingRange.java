package org.spigotmc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.ItemEntity;

public final class TrackingRange {

    private TrackingRange() {
    }

    /**
     * Gets the range an entity should be 'tracked' by players and visible in
     * the client.
     *
     * @param defaultRange Default range defined by Mojang
     */
    public static int getEntityTrackingRange(final Entity entity, final int defaultRange) {
        if (defaultRange == 0) {
            return defaultRange;
        }

        final SpigotWorldConfig config = entity.level().spigotConfig();
        if (entity instanceof ServerPlayer) {
            return config.playerTrackingRange;
        }

        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            // Exempt ender dragon
            return ((ServerLevel) entity.level()).getChunkSource().chunkMap.serverViewDistance << 4;
        }

        // the categories of Spigot's activation types, which Silkard does not track per entity
        if (entity instanceof net.minecraft.world.entity.monster.Enemy || entity instanceof net.minecraft.world.entity.raid.Raider) {
            return config.monsterTrackingRange;
        }
        if (entity instanceof net.minecraft.world.entity.PathfinderMob || entity instanceof net.minecraft.world.entity.ambient.AmbientCreature) {
            return config.animalTrackingRange;
        }

        if (entity instanceof ItemFrame || entity instanceof Painting || entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
            return config.miscTrackingRange;
        } else if (entity instanceof Display) {
            return config.displayTrackingRange;
        } else {
            return config.otherTrackingRange;
        }
    }
}
