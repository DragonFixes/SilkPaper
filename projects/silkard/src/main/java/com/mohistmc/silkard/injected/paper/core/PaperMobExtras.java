package com.mohistmc.silkard.injected.paper.core;

import java.util.UUID;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import org.bukkit.entity.ExperienceOrb;
import org.jspecify.annotations.Nullable;

/**
 * Small pieces of per-type entity state that Paper and Purpur add to individual vanilla entity
 * classes. Each nested interface is injected into the classes named in its documentation and
 * implemented by the matching {@code MixinPaper*} class in {@code com.mohistmc.silkard.mixin.paper.core}.
 */
public final class PaperMobExtras {

    private PaperMobExtras() {
    }

    /** {@code Mob}: Paper's override of whether the mob despawns in peaceful difficulty. */
    public interface Mob {
        default TriState paper$despawnInPeacefulOverride() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setDespawnInPeacefulOverride(TriState state) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code Bee}: Paper's rolling animation override. */
    public interface Bee {
        default TriState paper$rollingOverride() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setRollingOverride(TriState rolling) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code IronGolem}, {@code SnowGolem}, {@code CopperGolem}, {@code WitherBoss}: Purpur's summoner. */
    public interface Summonable {
        default @Nullable UUID paper$summoner() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setSummoner(@Nullable UUID summoner) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code FireworkRocketEntity}, {@code Phantom}: the entity whose action spawned this one (Paper). */
    public interface SpawnedBy {
        default @Nullable UUID paper$spawningEntity() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setSpawningEntity(@Nullable UUID spawningEntity) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code Llama}: Purpur's caravan switch. */
    public interface Llama {
        default boolean paper$shouldJoinCaravan() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setShouldJoinCaravan(boolean shouldJoinCaravan) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code WanderingTrader}: Paper's invisibility potion and milk switches. */
    public interface WanderingTrader {
        default boolean paper$canDrinkPotion() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setCanDrinkPotion(boolean canDrinkPotion) {
            throw new IllegalStateException("Not implemented");
        }

        default boolean paper$canDrinkMilk() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setCanDrinkMilk(boolean canDrinkMilk) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code ExperienceOrb}: Paper's orb origin data. */
    public interface ExperienceOrbData {
        default @Nullable UUID paper$triggerEntityId() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setTriggerEntityId(@Nullable UUID id) {
            throw new IllegalStateException("Not implemented");
        }

        default @Nullable UUID paper$sourceEntityId() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setSourceEntityId(@Nullable UUID id) {
            throw new IllegalStateException("Not implemented");
        }

        default ExperienceOrb.SpawnReason paper$orbSpawnReason() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setOrbSpawnReason(ExperienceOrb.SpawnReason reason) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code FallingBlockEntity}: Paper's auto-expire switch. */
    public interface FallingBlock {
        default boolean paper$autoExpire() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setAutoExpire(boolean autoExpire) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code Endermite}: Purpur's player-spawned flag. */
    public interface Endermite {
        default boolean paper$playerSpawned() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setPlayerSpawned(boolean playerSpawned) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code Wolf}: Purpur's rabid flag. */
    public interface Wolf {
        default boolean paper$rabid() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setRabid(boolean rabid) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code WitherBoss}: Paper's portal switch. */
    public interface WitherBoss {
        default void paper$setCanTravelThroughPortals(boolean canTravel) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code EnderDragon}: Paper's podium override. */
    public interface EnderDragon {
        default @Nullable BlockPos paper$podium() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setPodium(@Nullable BlockPos podium) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code AbstractCubeMob}: Paper's wander switch. */
    public interface CubeMob {
        default boolean paper$canWander() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setCanWander(boolean canWander) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code AbstractMinecart}: Paper's friction override. */
    public interface Minecart {
        default TriState paper$frictionState() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setFrictionState(TriState state) {
            throw new IllegalStateException("Not implemented");
        }
    }
}
