package com.mohistmc.silkard.mixin.paper.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.injected.paper.core.PaperLivingEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur daylight burning API: a per-mob override of the {@code burn_in_daylight} entity tag. */
@Mixin(Mob.class)
public abstract class MixinPaperMob {

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean paper$shouldBurnInDay(Mob self, TagKey<EntityType<?>> tag, Operation<Boolean> original) {
        Boolean override = ((PaperLivingEntity) self).paper$shouldBurnInDayOverride();
        return override != null ? override : original.call(self, tag);
    }
}
