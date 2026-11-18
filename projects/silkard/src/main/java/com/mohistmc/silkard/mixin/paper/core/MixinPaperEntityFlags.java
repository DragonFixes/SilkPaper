package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperMobExtras;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Plain Paper and Purpur flags on single vanilla entity classes. Each nested mixin holds the state
 * of one class; where noted, the flag is stored but vanilla behavior does not consult it yet.
 */
public final class MixinPaperEntityFlags {

    private MixinPaperEntityFlags() {
    }

    /** Paper's wandering trader potion and milk switches. */
    @Mixin(net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader.class)
    public abstract static class WanderingTrader implements PaperMobExtras.WanderingTrader {
        // TODO Silkard - the trader's UseItemGoal conditions do not consult these switches yet
        @Unique private boolean paper$canDrinkPotion = true;
        @Unique private boolean paper$canDrinkMilk = true;

        @Override
        public boolean paper$canDrinkPotion() {
            return this.paper$canDrinkPotion;
        }

        @Override
        public void paper$setCanDrinkPotion(boolean canDrinkPotion) {
            this.paper$canDrinkPotion = canDrinkPotion;
        }

        @Override
        public boolean paper$canDrinkMilk() {
            return this.paper$canDrinkMilk;
        }

        @Override
        public void paper$setCanDrinkMilk(boolean canDrinkMilk) {
            this.paper$canDrinkMilk = canDrinkMilk;
        }
    }

    /** Paper's falling block auto-expire switch. */
    @Mixin(net.minecraft.world.entity.item.FallingBlockEntity.class)
    public abstract static class FallingBlock implements PaperMobExtras.FallingBlock {
        // TODO Silkard - the falling block's 600 tick expiry does not consult this switch yet
        @Unique private boolean paper$autoExpire = true;

        @Override
        public boolean paper$autoExpire() {
            return this.paper$autoExpire;
        }

        @Override
        public void paper$setAutoExpire(boolean autoExpire) {
            this.paper$autoExpire = autoExpire;
        }
    }

    /** Purpur's player-spawned endermite flag. */
    @Mixin(net.minecraft.world.entity.monster.Endermite.class)
    public abstract static class Endermite implements PaperMobExtras.Endermite {
        @Unique private boolean paper$playerSpawned;

        @Override
        public boolean paper$playerSpawned() {
            return this.paper$playerSpawned;
        }

        @Override
        public void paper$setPlayerSpawned(boolean playerSpawned) {
            this.paper$playerSpawned = playerSpawned;
        }
    }

    /** Purpur's rabid wolf flag. */
    @Mixin(net.minecraft.world.entity.animal.wolf.Wolf.class)
    public abstract static class Wolf implements PaperMobExtras.Wolf {
        // TODO Silkard - Purpur's rabid wolf behavior (attacking, infection, cure) is not ported; this is plain state
        @Unique private boolean paper$rabid;

        @Override
        public boolean paper$rabid() {
            return this.paper$rabid;
        }

        @Override
        public void paper$setRabid(boolean rabid) {
            this.paper$rabid = rabid;
        }
    }

    /** Paper's wither portal switch: when set, the wither may travel through portals. */
    @Mixin(net.minecraft.world.entity.boss.wither.WitherBoss.class)
    public abstract static class WitherBoss implements PaperMobExtras.WitherBoss {
        @Unique private boolean paper$canTravelThroughPortals;

        @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
        private void paper$portalSwitch(boolean ignorePassenger, CallbackInfoReturnable<Boolean> cir) {
            if (this.paper$canTravelThroughPortals) {
                cir.setReturnValue(true);
            }
        }

        @Override
        public void paper$setCanTravelThroughPortals(boolean canTravel) {
            this.paper$canTravelThroughPortals = canTravel;
        }
    }

    /** Paper's ender dragon podium override. */
    @Mixin(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class)
    public abstract static class EnderDragon implements PaperMobExtras.EnderDragon {
        // TODO Silkard - the dragon's phases still path to the podium of the fight origin; the override is reported only
        @Unique private @Nullable BlockPos paper$podium;

        @Override
        public @Nullable BlockPos paper$podium() {
            return this.paper$podium;
        }

        @Override
        public void paper$setPodium(@Nullable BlockPos podium) {
            this.paper$podium = podium;
        }
    }

    /** Paper's slime and magma cube wander switch. */
    @Mixin(net.minecraft.world.entity.monster.cubemob.AbstractCubeMob.class)
    public abstract static class CubeMob implements PaperMobExtras.CubeMob {
        // TODO Silkard - the cube mob movement goals do not consult this switch yet
        @Unique private boolean paper$canWander = true;

        @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
        private void paper$save(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
            output.putBoolean("Paper.canWander", this.paper$canWander);
        }

        @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
        private void paper$load(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
            this.paper$canWander = input.getBooleanOr("Paper.canWander", true);
        }

        @Override
        public boolean paper$canWander() {
            return this.paper$canWander;
        }

        @Override
        public void paper$setCanWander(boolean canWander) {
            this.paper$canWander = canWander;
        }
    }

    /** Paper's minecart friction override. */
    @Mixin(net.minecraft.world.entity.vehicle.minecart.AbstractMinecart.class)
    public abstract static class Minecart implements PaperMobExtras.Minecart {
        // TODO Silkard - minecart movement does not apply Paper's friction override yet
        @Unique private TriState paper$frictionState = TriState.NOT_SET;

        @Override
        public TriState paper$frictionState() {
            return this.paper$frictionState;
        }

        @Override
        public void paper$setFrictionState(TriState state) {
            this.paper$frictionState = state;
        }
    }
}
