package com.mohistmc.silkard.mixin.paper.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.injected.paper.server.PaperBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class MixinPaperBeaconBlockEntity implements PaperBeaconBlockEntity {

    @Unique private static final String SILKARD$RANGE_KEY = "Paper.Range";
    // applyEffects is static and only receives the beacon's level, so tick() hands the beacon over here.
    @Unique private static final ThreadLocal<BeaconBlockEntity> SILKARD$APPLYING = new ThreadLocal<>();

    @Shadow private int levels;

    @Unique private double silkard$effectRange = -1;

    @Override
    public double getEffectRange() {
        return this.silkard$effectRange < 0 ? this.levels * 10 + 10 : this.silkard$effectRange;
    }

    @Override
    public void setEffectRange(double range) {
        this.silkard$effectRange = range;
    }

    @Override
    public void resetEffectRange() {
        this.silkard$effectRange = -1;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V"))
    private static void silkard$applyWithBeacon(Level level, BlockPos pos, int levels, @Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary, Operation<Void> original,
                                                Level tickLevel, BlockPos tickPos, BlockState state, BeaconBlockEntity entity) {
        SILKARD$APPLYING.set(entity);
        try {
            original.call(level, pos, levels, primary, secondary);
        } finally {
            SILKARD$APPLYING.remove();
        }
    }

    @ModifyVariable(method = "applyEffects", at = @At("STORE"), ordinal = 0)
    private static double silkard$customRange(double range) {
        BeaconBlockEntity beacon = SILKARD$APPLYING.get();
        return beacon == null ? range : ((PaperBeaconBlockEntity) beacon).getEffectRange();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void silkard$loadRange(ValueInput input, CallbackInfo ci) {
        this.silkard$effectRange = input.getDoubleOr(SILKARD$RANGE_KEY, -1);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void silkard$saveRange(ValueOutput output, CallbackInfo ci) {
        if (this.silkard$effectRange >= 0) {
            output.putDouble(SILKARD$RANGE_KEY, this.silkard$effectRange);
        }
    }
}
