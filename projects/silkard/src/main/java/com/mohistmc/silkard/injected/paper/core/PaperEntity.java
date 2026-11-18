package com.mohistmc.silkard.injected.paper.core;

import java.util.UUID;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jspecify.annotations.Nullable;

/**
 * Entity state that Paper and Purpur add to {@link net.minecraft.world.entity.Entity}.
 * Implemented by {@code com.mohistmc.silkard.mixin.paper.core.MixinPaperEntity}.
 */
public interface PaperEntity {

    /** Whether the current pose is locked against vanilla pose updates (Paper pose API). */
    default boolean paper$fixedPose() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setFixedPose(boolean fixedPose) {
        throw new IllegalStateException("Not implemented");
    }

    /** Purpur fire immunity override; {@code null} defers to the entity type. */
    default @Nullable Boolean paper$immuneToFire() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setImmuneToFire(@Nullable Boolean immuneToFire) {
        throw new IllegalStateException("Not implemented");
    }

    /** Visual fire override; {@link TriState#NOT_SET} shows fire only while burning. */
    default TriState paper$visualFire() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setVisualFire(TriState visualFire) {
        throw new IllegalStateException("Not implemented");
    }

    /** Whether freeze ticks are locked against vanilla thawing. */
    default boolean paper$freezeLocked() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setFreezeLocked(boolean freezeLocked) {
        throw new IllegalStateException("Not implemented");
    }

    /** The Bukkit cause of the entity's current removal, or {@code null} while not removed. */
    default EntityRemoveEvent.@Nullable Cause paper$removeEventCause() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setRemoveEventCause(EntityRemoveEvent.@Nullable Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    /** Ticks this entity has existed, persisted across saves (Bukkit ticks lived). */
    default int paper$totalEntityAge() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setTotalEntityAge(int age) {
        throw new IllegalStateException("Not implemented");
    }

    /** Position the entity was first added to a world at. */
    default @Nullable Vec3 paper$origin() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setOrigin(@Nullable Vec3 origin) {
        throw new IllegalStateException("Not implemented");
    }

    /** UID of the Bukkit world the entity was first added to. */
    default @Nullable UUID paper$originWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setOriginWorld(@Nullable UUID originWorld) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean paper$spawnedViaMobSpawner() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setSpawnedViaMobSpawner(boolean spawnedViaMobSpawner) {
        throw new IllegalStateException("Not implemented");
    }

    /** Reason the entity was spawned; {@code null} until the entity is added to a world or loaded. */
    default CreatureSpawnEvent.@Nullable SpawnReason paper$spawnReason() {
        throw new IllegalStateException("Not implemented");
    }

    default void paper$setSpawnReason(CreatureSpawnEvent.@Nullable SpawnReason spawnReason) {
        throw new IllegalStateException("Not implemented");
    }
}
