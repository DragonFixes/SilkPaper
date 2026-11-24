package com.mohistmc.silkard.mixin.purpur.server.level;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Purpur - AFK API: any player action ends AFK, and AFK players cannot be collided with. */
@Mixin(ServerPlayer.class)
public abstract class MixinPurpurServerPlayerAfk extends Player {

    private MixinPurpurServerPlayerAfk(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "resetLastActionTime", at = @At("TAIL"))
    private void purpur$endAfk(CallbackInfo ci) {
        ((ServerPlayer) (Object) this).paper$setAfk(false);
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return !((ServerPlayer) (Object) this).paper$afk() && super.canBeCollidedWith(entity);
    }
}
