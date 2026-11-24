package com.mohistmc.silkard.mixin.vanilla.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CraftBukkit - EntityBreakDoorEvent: a cancelled break restarts the mob's door breaking. */
@Mixin(BreakDoorGoal.class)
public abstract class MixinBreakDoorGoal extends DoorInteractGoal {

    private MixinBreakDoorGoal(Mob mob) {
        super(mob);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"), cancellable = true, require = 1)
    private void silkard$breakDoorEvent(CallbackInfo ci) {
        // Paper - the door becomes the fluid at its position, not always air
        if (CraftEventFactory.callEntityBreakDoorEvent(this.mob, this.doorPos, this.mob.level().getFluidState(this.doorPos).createLegacyBlock()).isCancelled()) {
            this.start();
            ci.cancel();
        }
    }
}
