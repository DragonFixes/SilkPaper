package com.mohistmc.silkard.mixin.paper.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mohistmc.silkard.injected.paper.core.PaperPlayer;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paper's flying fall damage switch: a player allowed to fly normally takes no fall damage, and
 * {@link TriState#TRUE} makes them take it anyway.
 */
@Mixin(Player.class)
public abstract class MixinPaperPlayer implements PaperPlayer {

    @Unique private TriState paper$flyingFallDamage = TriState.NOT_SET;

    @ModifyExpressionValue(method = "causeFallDamage", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z"))
    private boolean paper$flyingFallDamage(boolean mayfly) {
        return mayfly && !this.paper$flyingFallDamage.toBooleanOrElse(false);
    }

    @Override
    public TriState paper$flyingFallDamage() {
        return this.paper$flyingFallDamage;
    }

    @Override
    public void paper$setFlyingFallDamage(TriState flyingFallDamage) {
        this.paper$flyingFallDamage = flyingFallDamage;
    }
}
