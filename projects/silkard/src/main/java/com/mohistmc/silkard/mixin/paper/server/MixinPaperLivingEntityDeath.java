package com.mohistmc.silkard.mixin.paper.server;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mohistmc.silkard.paper.server.DeathCapture;
import com.mohistmc.silkard.paper.server.DefaultDrop;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * CraftBukkit's EntityDeathEvent, with Paper's restored vanilla drops: the drops a dying entity
 * makes are captured, shown to the event, and spawned afterwards with the experience, unless a
 * plugin cancels the death and revives the entity.
 *
 * <p>The priority is below the default so other mods' injections into {@code die}, such as
 * Fabric API's kill and death events, apply on top of the overwrite.
 */
@Mixin(value = LivingEntity.class, priority = 500)
public abstract class MixinPaperLivingEntityDeath implements DeathCapture {

    @Shadow @Final private static Logger LOGGER;
    @Shadow protected boolean dead;

    @Shadow public abstract @Nullable LivingEntity getKillCredit();
    @Shadow public abstract boolean isSleeping();
    @Shadow public abstract void stopSleeping();
    @Shadow public abstract void stopUsingItem();
    @Shadow public abstract net.minecraft.world.damagesource.CombatTracker getCombatTracker();
    @Shadow protected abstract void handleKillingBlow();
    @Shadow protected abstract void dropAllDeathLoot(ServerLevel level, DamageSource source);
    @Shadow protected abstract void createWitherRose(@Nullable LivingEntity killer);
    @Shadow public abstract void setHealth(float health);

    @Unique private @Nullable List<DefaultDrop> silkard$deathDropItems;
    @Unique private final List<Runnable> silkard$postDeathEventTasks = new ArrayList<>();

    @Override
    public @Nullable List<DefaultDrop> silkard$deathDropItems() {
        return this.silkard$deathDropItems;
    }

    @Override
    public void silkard$setDeathDropItems(@Nullable List<DefaultDrop> drops) {
        this.silkard$deathDropItems = drops;
    }

    @Override
    public List<Runnable> silkard$postDeathEventTasks() {
        return this.silkard$postDeathEventTasks;
    }

    /**
     * @author SilkPaper
     * @reason CraftBukkit EntityDeathEvent; Paper restored vanilla drops and cancellable deaths
     */
    @Overwrite
    public void die(DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.isRemoved() || this.dead) {
            return;
        }
        Entity sourceEntity = source.getEntity();
        LivingEntity killer = this.getKillCredit();
        Runnable afterEvent = () -> { // Paper
            if (killer != null) {
                killer.awardKillScore(self, source);
            }
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            this.stopUsingItem();
            if (!self.level().isClientSide() && self.hasCustomName()) {
                if (org.spigotmc.SpigotConfig.logNamedDeaths) LOGGER.info("Named entity {} died: {}", self, this.getCombatTracker().getDeathMessage().getString()); // Spigot
            }
            this.handleKillingBlow();
        };
        if (self.level() instanceof ServerLevel serverLevel) {
            // Paper start
            this.silkard$deathDropItems = new ArrayList<>();
            this.dropAllDeathLoot(serverLevel, source);
            org.bukkit.event.entity.EntityDeathEvent deathEvent = CraftEventFactory.callEntityDeathEvent(serverLevel, self, source, this.silkard$deathDropItems, true);
            this.silkard$deathDropItems = null;
            if (deathEvent.isCancelled()) {
                this.dead = false;
                this.silkard$postDeathEventTasks.clear();
                this.setHealth((float) deathEvent.getReviveHealth());
                return;
            }
            afterEvent.run();
            // Paper end

            if (sourceEntity == null || sourceEntity.killedEntity(serverLevel, self, source)) {
                self.gameEvent(GameEvent.ENTITY_DIE);
                this.silkard$postDeathEventTasks.forEach(Runnable::run); // Paper
                this.createWitherRose(killer);
            }
            this.silkard$postDeathEventTasks.clear(); // Paper
            self.level().broadcastEntityEvent(self, EntityEvent.DEATH);
        } else {
            afterEvent.run();
        }

        self.setPose(Pose.DYING);
    }

    // Paper - experience drops after the death event, which may change or cancel it
    @WrapWithCondition(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V"), require = 1)
    private boolean silkard$delayExperience(LivingEntity self, ServerLevel level, @Nullable Entity killer) {
        if (this.silkard$deathDropItems == null) {
            return true;
        }
        this.silkard$postDeathEventTasks.add(() -> ((MixinPaperLivingEntityDeathInvoker) self).silkard$dropExperience(level, killer));
        return false;
    }
}
