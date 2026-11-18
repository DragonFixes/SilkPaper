package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paper entity origin API: the first world and position an entity is tracked in. */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class MixinPaperServerLevelEntityCallbacks {

    @Shadow @Final ServerLevel this$0;

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void paper$recordOrigin(Entity entity, CallbackInfo ci) {
        PaperEntity paper = (PaperEntity) entity;
        if (paper.paper$origin() == null) {
            paper.paper$setOrigin(entity.position());
        }
        // Entities rarely change worlds before their first save, so the tracking world stands in for an unknown origin world.
        if (paper.paper$originWorld() == null) {
            paper.paper$setOriginWorld(this.this$0.getWorld().getUID());
        }
    }
}
