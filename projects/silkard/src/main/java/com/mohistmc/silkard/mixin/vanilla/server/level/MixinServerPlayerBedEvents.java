package com.mohistmc.silkard.mixin.vanilla.server.level;

import com.mohistmc.silkard.paper.server.PaperServerHooks;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CraftBukkit/Paper bed handling for players.
 *
 * <p>The checks vanilla runs inline in {@code startSleepInBed} are split out into
 * {@link #silkard$getBedResult}, so PlayerBedEnterEvent sees every outcome (including the bed
 * exploding outside the Overworld, reported as {@link PaperServerHooks#BED_EXPLOSION}) and plugins
 * can allow or deny sleep. Only {@code OTHER_PROBLEM} (already asleep, or dead) cannot be overridden.
 * The {@code force} variant backs {@code HumanEntity#sleep(Location, boolean)}.
 *
 * <p>Leaving a bed fires PlayerBedLeaveEvent; cancelling it keeps the player in bed.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerBedEvents extends Player {

    // @formatter:off
    @Shadow private boolean bedInRange(BlockPos pos, Direction direction) { throw new AssertionError(); }
    @Shadow private boolean bedBlocked(BlockPos pos, Direction direction) { throw new AssertionError(); }
    // @formatter:on

    public MixinServerPlayerBedEvents(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true, require = 1)
    private void silkard$bedEnterEvent(AbstractBedBlock bedBlock, BlockState bedBlockState, BedRule rule, BlockPos pos,
                                       CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        cir.setReturnValue(this.startSleepInBed(bedBlock, bedBlockState, rule, pos, false));
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(AbstractBedBlock bedBlock, BlockState bedBlockState, BedRule rule, BlockPos pos, boolean force) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        Direction direction = bedBlockState.getValue(HorizontalDirectionalBlock.FACING);
        Either<Player.BedSleepingProblem, Unit> bedResult = this.silkard$getBedResult(self, pos, direction, rule);
        boolean canSetSpawn = rule.canSetSpawn(this.level());

        if (bedResult.left().orElse(null) == Player.BedSleepingProblem.OTHER_PROBLEM) {
            return bedResult; // not bypassable by plugins
        }

        if (force) {
            bedResult = Either.right(Unit.INSTANCE);
        }

        bedResult = CraftEventFactory.callPlayerBedEnterEvent(self, pos, rule, bedResult);
        if (bedResult.left().isPresent()) {
            return bedResult;
        }

        Either<Player.BedSleepingProblem, Unit> result = super.startSleepInBed(bedBlock, bedBlockState, rule, pos).ifRight(unit -> {
            this.awardStat(bedBlock.getSleptInBedStatType());
            if (canSetSpawn) {
                CriteriaTriggers.SLEPT_IN_BED.trigger(self);
            }
        });
        if (!self.level().canSleepThroughNights()) {
            // Purpur - Customizable sleeping actionbar messages
            Component clientMessage;
            if (org.purpurmc.purpur.PurpurConfig.sleepNotPossible.isBlank()) {
                clientMessage = null;
            } else if (!org.purpurmc.purpur.PurpurConfig.sleepNotPossible.equalsIgnoreCase("default")) {
                clientMessage = io.papermc.paper.adventure.PaperAdventure.asVanilla(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(org.purpurmc.purpur.PurpurConfig.sleepNotPossible));
            } else {
                clientMessage = Component.translatable("sleep.not_possible");
            }
            if (clientMessage != null) {
                this.sendOverlayMessage(clientMessage);
            }
        }

        self.level().updateSleepingPlayerList();
        return result;
    }

    @Unique
    private Either<Player.BedSleepingProblem, Unit> silkard$getBedResult(ServerPlayer self, BlockPos pos, Direction direction, BedRule rule) {
        if (rule.destroyOnUse()) {
            return Either.left(PaperServerHooks.BED_EXPLOSION);
        }
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        boolean canSleep = rule.canSleep(this.level());
        boolean canSetSpawn = rule.canSetSpawn(this.level());
        if (!this.bedInRange(pos, direction)) {
            return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
        }
        if (this.bedBlocked(pos, direction)) {
            return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
        }
        if (canSetSpawn) {
            self.setRespawnPosition(
                new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(self.level().dimension(), pos, this.getYRot(), this.getXRot()), false),
                true, PlayerSpawnChangeEvent.Cause.BED);
        }
        if (!canSleep) {
            return Either.left(rule.asProblem());
        }
        if (!this.isCreative()) {
            Vec3 bedCenter = Vec3.atBottomCenterOf(pos);
            List<Monster> monsters = self.level().getEntitiesOfClass(
                Monster.class,
                new AABB(bedCenter.x() - 8.0, bedCenter.y() - 5.0, bedCenter.z() - 8.0, bedCenter.x() + 8.0, bedCenter.y() + 5.0, bedCenter.z() + 8.0),
                monster -> monster.isPreventingPlayerRest(self.level(), self)
            );
            if (!self.level().purpurConfig().playerSleepNearMonsters && !monsters.isEmpty()) { // Purpur - Config to ignore nearby mobs when sleeping
                return Either.left(Player.BedSleepingProblem.NOT_SAFE);
            }
        }
        return Either.right(Unit.INSTANCE);
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true, require = 1)
    private void silkard$bedLeaveEvent(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        if (!this.isSleeping()) {
            ci.cancel(); // can't leave a bed the player is not in
            return;
        }
        org.bukkit.block.Block bed = CraftBlock.at(this.level(), this.getSleepingPos().orElse(this.blockPosition()));
        PlayerBedLeaveEvent event = new PlayerBedLeaveEvent((org.bukkit.entity.Player) this.getBukkitEntity(), bed, true);
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
