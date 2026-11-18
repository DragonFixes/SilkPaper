package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperServerPlayerAccess;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Purpur's per-player boss bar toggles (saved with the player) and Paper's Adventure display name.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinPaperServerPlayer implements PaperServerPlayerAccess {

    @Unique private boolean paper$tpsBar;
    @Unique private boolean paper$compassBar;
    @Unique private boolean paper$ramBar;
    @Unique private @Nullable Component paper$adventureDisplayName;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$readBars(final ValueInput input, final CallbackInfo ci) {
        this.paper$tpsBar = input.getBooleanOr("Purpur.TPSBar", false);
        this.paper$compassBar = input.getBooleanOr("Purpur.CompassBar", false);
        this.paper$ramBar = input.getBooleanOr("Purpur.RamBar", false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$saveBars(final ValueOutput output, final CallbackInfo ci) {
        output.putBoolean("Purpur.TPSBar", this.paper$tpsBar);
        output.putBoolean("Purpur.CompassBar", this.paper$compassBar);
        output.putBoolean("Purpur.RamBar", this.paper$ramBar);
    }

    @Override
    public boolean tpsBar() {
        return this.paper$tpsBar;
    }

    @Override
    public void tpsBar(final boolean tpsBar) {
        this.paper$tpsBar = tpsBar;
    }

    @Override
    public boolean compassBar() {
        return this.paper$compassBar;
    }

    @Override
    public void compassBar(final boolean compassBar) {
        this.paper$compassBar = compassBar;
    }

    @Override
    public boolean ramBar() {
        return this.paper$ramBar;
    }

    @Override
    public void ramBar(final boolean ramBar) {
        this.paper$ramBar = ramBar;
    }

    @Override
    public @Nullable Component paper$adventureDisplayName() {
        return this.paper$adventureDisplayName;
    }

    @Override
    public void paper$adventureDisplayName(final @Nullable Component displayName) {
        this.paper$adventureDisplayName = displayName;
    }
}
