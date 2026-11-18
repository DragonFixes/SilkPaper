package com.mohistmc.silkard.paper.paper;

import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;

/**
 * The two halves Paper splits {@link CombatTracker#recordDamage} and {@link CombatTracker#recheckStatus}
 * into, so the combat tracker API can add entries and reset state directly.
 */
public final class CombatTrackers {

    private CombatTrackers() {
    }

    /** Records {@code entry} and enters combat as vanilla does for a freshly recorded hit. */
    public static void recordDamageAndCheckCombatState(final CombatTracker tracker, final CombatEntry entry) {
        tracker.recheckStatus();
        tracker.entries.add(entry);
        tracker.lastDamageTime = tracker.mob.tickCount;
        tracker.takingDamage = true;
        if (!tracker.inCombat && tracker.mob.isAlive() && entry.source().getEntity() instanceof LivingEntity) {
            tracker.inCombat = true;
            tracker.combatStartTime = tracker.mob.tickCount;
            tracker.combatEndTime = tracker.combatStartTime;
            tracker.mob.onEnterCombat();
        }
    }

    /** Leaves combat and forgets all entries, as vanilla does once the combat timeout passes. */
    public static void resetCombatState(final CombatTracker tracker) {
        final boolean wasInCombat = tracker.inCombat;
        tracker.takingDamage = false;
        tracker.inCombat = false;
        tracker.combatEndTime = tracker.mob.tickCount;
        if (wasInCombat) {
            tracker.mob.onLeaveCombat();
        }
        tracker.entries.clear();
    }
}
