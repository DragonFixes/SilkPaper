package com.mohistmc.silkard.mixin.vanilla.server.level;

import com.mohistmc.silkard.injected.world.entity.ContextEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CraftBukkit's {@code Entity.valid}: an entity is valid while its level tracks it. Bukkit's
 * {@code Entity.isValid()}, entity teleports from plugins, and the events that are skipped for
 * entities still being generated or loaded all read it.
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class MixinServerLevel$EntityCallbacks {

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), require = 1)
    private void silkard$markValid(Entity entity, CallbackInfo ci) {
        ((ContextEntity) entity).silkard_valid(true);
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"), require = 1)
    private void silkard$markInvalid(Entity entity, CallbackInfo ci) {
        ((ContextEntity) entity).silkard_valid(false);
    }
}
