package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.EquipmentSlot;

public class CraftFishHook extends CraftProjectile implements FishHook {

    private double biteChance = -1;

    public CraftFishHook(CraftServer server, FishingHook entity) {
        super(server, entity);
    }

    @Override
    public FishingHook getHandle() {
        return (FishingHook) this.entity;
    }

    @Override
    public int getMinWaitTime() {
<<<<<<< silkard
        return getHandle().silkard$minWaitTime();
=======
        return this.getHandle().minWaitTime;
>>>>>>> paper
    }

    @Override
    public void setMinWaitTime(int minWaitTime) {
        Preconditions.checkArgument(minWaitTime >= 0 && minWaitTime <= this.getMaxWaitTime(), "The minimum wait time should be between %s and %s (the maximum wait time)", 0, this.getMaxWaitTime());
<<<<<<< silkard
        FishingHook hook = getHandle();
        hook.silkard$minWaitTime(minWaitTime);
=======
        FishingHook hook = this.getHandle();
        hook.minWaitTime = minWaitTime;
>>>>>>> paper
    }

    @Override
    public int getMaxWaitTime() {
<<<<<<< silkard
        return getHandle().silkard$maxWaitTime();
=======
        return this.getHandle().maxWaitTime;
>>>>>>> paper
    }

    @Override
    public void setMaxWaitTime(int maxWaitTime) {
        Preconditions.checkArgument(maxWaitTime >= 0 && maxWaitTime >= this.getMinWaitTime(), "The maximum wait time should be between %s and %s (the minimum wait time)", 0, this.getMinWaitTime());
<<<<<<< silkard
        FishingHook hook = getHandle();
        hook.silkard$maxWaitTime(maxWaitTime);
=======
        FishingHook hook = this.getHandle();
        hook.maxWaitTime = maxWaitTime;
>>>>>>> paper
    }

    @Override
    public void setWaitTime(int min, int max) {
        Preconditions.checkArgument(min >= 0 && max >= 0 && min <= max, "The minimum/maximum wait time should be higher than or equal to 0 and the minimum wait time");
<<<<<<< silkard
        getHandle().silkard$minWaitTime(min);
        getHandle().silkard$maxWaitTime(max);
=======
        this.getHandle().minWaitTime = min;
        this.getHandle().maxWaitTime = max;
>>>>>>> paper
    }

    @Override
    public int getMinLureTime() {
<<<<<<< silkard
        return getHandle().silkard$minLureTime();
=======
        return this.getHandle().minLureTime;
>>>>>>> paper
    }

    @Override
    public void setMinLureTime(int minLureTime) {
        Preconditions.checkArgument(minLureTime >= 0 && minLureTime <= this.getMaxLureTime(), "The minimum lure time (%s) should be between 0 and %s (the maximum wait time)", minLureTime, this.getMaxLureTime());
<<<<<<< silkard
        getHandle().silkard$minLureTime(minLureTime);
=======
        this.getHandle().minLureTime = minLureTime;
>>>>>>> paper
    }

    @Override
    public int getMaxLureTime() {
<<<<<<< silkard
        return getHandle().silkard$maxLureTime();
=======
        return this.getHandle().maxLureTime;
>>>>>>> paper
    }

    @Override
    public void setMaxLureTime(int maxLureTime) {
        Preconditions.checkArgument(maxLureTime >= 0 && maxLureTime >= this.getMinLureTime(), "The maximum lure time (%s) should be higher than or equal to 0 and %s (the minimum wait time)", maxLureTime, this.getMinLureTime());
<<<<<<< silkard
        getHandle().silkard$maxLureTime(maxLureTime);
=======
        this.getHandle().maxLureTime = maxLureTime;
>>>>>>> paper
    }

    @Override
    public void setLureTime(int min, int max) {
        Preconditions.checkArgument(min >= 0 && max >= 0 && min <= max, "The minimum/maximum lure time should be higher than or equal to 0 and the minimum wait time.");
<<<<<<< silkard
        getHandle().silkard$minLureTime(min);
        getHandle().silkard$maxLureTime(max);
=======
        this.getHandle().minLureTime = min;
        this.getHandle().maxLureTime = max;
>>>>>>> paper
    }

    @Override
    public float getMinLureAngle() {
<<<<<<< silkard
        return getHandle().silkard$minLureAngle();
=======
        return this.getHandle().minLureAngle;
>>>>>>> paper
    }

    @Override
    public void setMinLureAngle(float minLureAngle) {
        Preconditions.checkArgument(minLureAngle <= this.getMaxLureAngle(), "The minimum lure angle (%s) should be less than %s (the maximum lure angle)", minLureAngle, this.getMaxLureAngle());
<<<<<<< silkard
        getHandle().silkard$minLureAngle(minLureAngle);
=======
        this.getHandle().minLureAngle = minLureAngle;
>>>>>>> paper
    }

    @Override
    public float getMaxLureAngle() {
<<<<<<< silkard
        return getHandle().silkard$maxLureAngle();
=======
        return this.getHandle().maxLureAngle;
>>>>>>> paper
    }

    @Override
    public void setMaxLureAngle(float maxLureAngle) {
        Preconditions.checkArgument(maxLureAngle >= this.getMinLureAngle(), "The minimum lure angle (%s) should be less than %s (the maximum lure angle)", maxLureAngle, this.getMinLureAngle());
<<<<<<< silkard
        getHandle().silkard$maxLureAngle(maxLureAngle);
=======
        this.getHandle().maxLureAngle = maxLureAngle;
>>>>>>> paper
    }

    @Override
    public void setLureAngle(float min, float max) {
        Preconditions.checkArgument(min <= max, "The minimum lure (%s) angle should be less than the maximum lure angle (%s)", min, max);
<<<<<<< silkard
        getHandle().silkard$minLureAngle(min);
        getHandle().silkard$maxLureAngle(max);
=======
        this.getHandle().minLureAngle = min;
        this.getHandle().maxLureAngle = max;
>>>>>>> paper
    }

    @Override
    public boolean isSkyInfluenced() {
<<<<<<< silkard
        return getHandle().silkard$skyInfluenced();
=======
        return this.getHandle().skyInfluenced;
>>>>>>> paper
    }

    @Override
    public void setSkyInfluenced(boolean skyInfluenced) {
<<<<<<< silkard
        getHandle().silkard$skyInfluenced(skyInfluenced);
=======
        this.getHandle().skyInfluenced = skyInfluenced;
>>>>>>> paper
    }

    @Override
    public boolean isRainInfluenced() {
<<<<<<< silkard
        return getHandle().silkard$rainInfluenced();
=======
        return this.getHandle().rainInfluenced;
>>>>>>> paper
    }

    @Override
    public void setRainInfluenced(boolean rainInfluenced) {
<<<<<<< silkard
        getHandle().silkard$rainInfluenced(rainInfluenced);
=======
        this.getHandle().rainInfluenced = rainInfluenced;
>>>>>>> paper
    }

    @Override
    public boolean getApplyLure() {
<<<<<<< silkard
        return getHandle().silkard$applyLure();
=======
        return this.getHandle().applyLure;
>>>>>>> paper
    }

    @Override
    public void setApplyLure(boolean applyLure) {
<<<<<<< silkard
        getHandle().silkard$applyLure(applyLure);
=======
        this.getHandle().applyLure = applyLure;
>>>>>>> paper
    }

    @Override
    public double getBiteChance() {
        FishingHook hook = this.getHandle();

        if (this.biteChance == -1) {
            if (hook.level().isRainingAt(BlockPos.containing(hook.position()).offset(0, 1, 0))) {
                return 1 / 300.0;
            }
            return 1 / 500.0;
        }
        return this.biteChance;
    }

    @Override
    public void setBiteChance(double chance) {
        Preconditions.checkArgument(chance >= 0 && chance <= 1, "The bite chance must be between 0 and 1");
        this.biteChance = chance;
    }

    @Override
    public boolean isInOpenWater() {
        return this.getHandle().outOfWaterTime < 10 && this.getHandle().calculateOpenWater(this.getHandle().blockPosition()); // Paper - isOpenWaterFishing is only calculated when a "fish" is approaching the hook
    }

    @Override
    public Entity getHookedEntity() {
        net.minecraft.world.entity.Entity hooked = this.getHandle().getHookedIn();
        return (hooked != null) ? hooked.getBukkitEntity() : null;
    }

    @Override
    public void setHookedEntity(Entity entity) {
        this.getHandle().setHookedEntity(entity != null ? ((CraftEntity) entity).getHandle() : null);
    }

    @Override
    public boolean pullHookedEntity() {
        FishingHook hook = this.getHandle();
        if (hook.getHookedIn() == null) {
            return false;
        }

        hook.pullEntity(hook.getHookedIn());
        return true;
    }

    @Override
    public HookState getState() {
        return HookState.values()[this.getHandle().currentState.ordinal()];
    }

    @Override
    public int getWaitTime() {
        return this.getHandle().timeUntilLured;
    }

    @Override
    public void setWaitTime(int ticks) {
        this.getHandle().timeUntilLured = ticks;
    }

    @Override
    public int getTimeUntilBite() {
        return this.getHandle().timeUntilHooked;
    }

    @Override
    public void setTimeUntilBite(final int ticks) {
        com.google.common.base.Preconditions.checkArgument(ticks >= 1, "Cannot set time until bite to less than 1 (%s<1)", ticks);
        final FishingHook hook = this.getHandle();

        // Reset the fish angle hook only when this call "enters" the fish into the lure stage.
        final boolean alreadyInLuringPhase = hook.timeUntilHooked > 0 && hook.timeUntilLured <= 0;
        if (!alreadyInLuringPhase) {
            hook.fishAngle = net.minecraft.util.Mth.nextFloat(hook.getRandom(), hook.minLureAngle, hook.maxLureAngle);
            hook.timeUntilLured = 0;
        }

        hook.timeUntilHooked = ticks;
    }

    @Override
    public void resetFishingState() {
        final FishingHook hook = this.getHandle();
        hook.resetTimeUntilLured();
        hook.timeUntilHooked = 0; // Reset time until hooked, will be repopulated once lured time is ticked down.
    }

    @Override
    public int retrieve(EquipmentSlot slot) {
        Preconditions.checkArgument(slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND, "Equipment slot must be HAND or OFF_HAND");
        final FishingHook fishingHook = getHandle();
        final Player playerOwner = fishingHook.getPlayerOwner();
        Preconditions.checkState(playerOwner != null, "Player owner cannot be null");

        final InteractionHand hand = CraftEquipmentSlot.getHand(slot);
        final ItemStack itemInHand = playerOwner.getItemInHand(hand);
        Preconditions.checkState(itemInHand.is(Items.FISHING_ROD), "Item in slot is not a FISHING_ROD");

        return fishingHook.retrieve(itemInHand, hand);
    }
}
