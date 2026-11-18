package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperArmorStand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's armor stand move and tick switches. ArmorStand inherits {@code move} and {@code tick},
 * so the checks live in {@code MixinPaperEntity} and {@code MixinPaperLivingEntity}; this mixin
 * holds the state and saves an API tick override under Paper's key.
 */
@Mixin(ArmorStand.class)
public abstract class MixinPaperArmorStand implements PaperArmorStand {

    @Unique private boolean paper$canMove = true;
    @Unique private boolean paper$canTick = true;
    @Unique private boolean paper$canTickSetByApi;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void paper$save(ValueOutput output, CallbackInfo ci) {
        if (this.paper$canTickSetByApi) {
            output.putBoolean("Paper.CanTickOverride", this.paper$canTick);
        }
        if (!this.paper$canMove) {
            output.putBoolean("Paper.CanMove", false);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void paper$load(ValueInput input, CallbackInfo ci) {
        input.read("Paper.CanTickOverride", com.mojang.serialization.Codec.BOOL).ifPresent(canTick -> {
            this.paper$canTick = canTick;
            this.paper$canTickSetByApi = true;
        });
        this.paper$canMove = input.getBooleanOr("Paper.CanMove", true);
    }

    @Override
    public boolean paper$canMove() {
        return this.paper$canMove;
    }

    @Override
    public void paper$setCanMove(boolean canMove) {
        this.paper$canMove = canMove;
    }

    @Override
    public boolean paper$canTick() {
        return this.paper$canTick;
    }

    @Override
    public void paper$setCanTick(boolean canTick) {
        this.paper$canTick = canTick;
        this.paper$canTickSetByApi = true;
    }
}
