package com.mohistmc.silkard.mixin.purpur.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - AFK API: what AFK players no longer count for. */
public final class MixinPurpurAfkTargeting {

    private MixinPurpurAfkTargeting() {
    }

    /** With idle-timeout target-player off, mobs do not target AFK players. */
    @Mixin(TargetingConditions.class)
    public abstract static class Targeting {
        @Inject(method = "test", at = @At("HEAD"), cancellable = true)
        private void purpur$ignoreAfk(ServerLevel level, @Nullable LivingEntity targeter, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
            if (target instanceof ServerPlayer player && player.paper$afk() && !level.purpurConfig().idleTimeoutTargetPlayer) {
                cir.setReturnValue(false);
            }
        }
    }

    /** AFK players do not keep mobs from despawning or let spawners run. */
    @Mixin(EntityGetter.class)
    public interface NearbyPlayers {
        @ModifyExpressionValue(method = "hasNearbyAlivePlayer", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z", ordinal = 1), require = 1)
        private boolean purpur$notAfk(boolean alive, @Local Player player) {
            return alive && !(player instanceof ServerPlayer serverPlayer && serverPlayer.paper$afk());
        }
    }
}
