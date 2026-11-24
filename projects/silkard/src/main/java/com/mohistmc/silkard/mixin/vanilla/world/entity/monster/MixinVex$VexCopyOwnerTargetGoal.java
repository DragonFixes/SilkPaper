package com.mohistmc.silkard.mixin.vanilla.world.entity.monster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A vex copying its owner's target fires EntityTargetEvent with OWNER_ATTACKED_TARGET (CraftBukkit).
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexCopyOwnerTargetGoal")
public class MixinVex$VexCopyOwnerTargetGoal {

    @Redirect(method = "start", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Vex;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void silkard$ownerTargetReason(Vex vex, LivingEntity target) {
        vex.setTarget(target, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
    }
}
