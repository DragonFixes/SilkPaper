package com.mohistmc.silkard.mixin.vanilla.server.level;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CraftBukkit - fire PlayerLevelChangeEvent once per tick when a player's level changed. */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerLevelChange {

    @Unique private int silkard$oldLevel = -1;

    @Inject(method = "doTick", at = @At("TAIL"))
    private void silkard$levelChange(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (this.silkard$oldLevel == -1) {
            this.silkard$oldLevel = self.experienceLevel;
        }
        if (this.silkard$oldLevel != self.experienceLevel) {
            CraftEventFactory.callPlayerLevelChangeEvent(self.getBukkitEntity(), this.silkard$oldLevel, self.experienceLevel);
            this.silkard$oldLevel = self.experienceLevel;
        }
    }
}
