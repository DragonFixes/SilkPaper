package com.mohistmc.silkard.mixin.vanilla.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.injected.world.entity.ContextEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CraftBukkit's VehicleEnterEvent / EntityMountEvent when an entity starts riding, and
 * VehicleExitEvent / EntityDismountEvent when it stops. The vehicle events fire only for living
 * riders of a Bukkit {@link Vehicle}; the mount events fire for every rider. Entities that are not
 * yet valid (world generation, chunk loading) mount and dismount without events.
 */
@Mixin(Entity.class)
public abstract class MixinEntityRidingEvents {

    // @formatter:off
    @Shadow private @Nullable Entity vehicle;
    @Shadow public abstract Level level();
    // @formatter:on

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("HEAD"), cancellable = true)
    private void silkard$rideOtherWorld(Entity entityToRide, boolean force, boolean sendEventAndTriggers, CallbackInfoReturnable<Boolean> cir) {
        // A plugin can ask an entity to ride one in another world; the passenger list would then
        // span two levels.
        if (entityToRide.level() != this.level()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Runs once the ride is allowed and before the old vehicle is left, so a cancelled mount keeps
     * the entity where it was.
     */
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z", ordinal = 0),
            cancellable = true, require = 1)
    private void silkard$mountEvents(Entity entityToRide, boolean force, boolean sendEventAndTriggers, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!((ContextEntity) self).silkard_valid()) {
            return;
        }
        if (entityToRide.getBukkitEntity() instanceof Vehicle bukkitVehicle && self instanceof LivingEntity entered) {
            VehicleEnterEvent event = new VehicleEnterEvent(bukkitVehicle, entered.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
                return;
            }
        }
        EntityMountEvent event = new EntityMountEvent(self.getBukkitEntity(), entityToRide.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * {@code removeVehicle} clears {@code vehicle} before it calls {@code removePassenger}, so a
     * cancelled dismount skips that call (and the passenger-side overrides of subclasses such as
     * the happy ghast) and puts the vehicle back.
     */
    @WrapOperation(method = "removeVehicle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V"),
            require = 1)
    private void silkard$dismountEvents(Entity oldVehicle, Entity passenger, Operation<Void> original) {
        // A listener that mounts the passenger on something else has already given it a new
        // vehicle; it still has to leave the old one's passenger list.
        if (((ContextEntity) oldVehicle).silkard_valid() && !silkard$allowDismount(oldVehicle, passenger) && passenger.getVehicle() == null) {
            this.vehicle = oldVehicle;
            return;
        }
        original.call(oldVehicle, passenger);
    }

    @Unique
    private static boolean silkard$allowDismount(Entity oldVehicle, Entity passenger) {
        if (oldVehicle.getBukkitEntity() instanceof Vehicle bukkitVehicle && passenger instanceof LivingEntity exited) {
            VehicleExitEvent event = new VehicleExitEvent(bukkitVehicle, (org.bukkit.entity.LivingEntity) exited.getBukkitEntity(), true);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled() || passenger.getVehicle() != null) {
                return false;
            }
        }
        EntityDismountEvent event = new EntityDismountEvent(passenger.getBukkitEntity(), oldVehicle.getBukkitEntity(), true);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}
