package com.mohistmc.silkard.mixin.vanilla.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mohistmc.silkard.bukkit.BukkitUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mgazul
 * @date 2026/10/29
 */
@Mixin(LayeredCauldronBlock.class)
public abstract class MixinLayeredCauldronBlock {

    @Shadow
    @Final
    private Biome.Precipitation precipitationType;

    @WrapOperation(method = "handlePrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean silkard$handlePrecipitation(Level level, BlockPos pos, BlockState newState, Operation<Boolean> original) {
        return BukkitUtils.changeCauldronLevel(level, pos, newState, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL);
    }

    @Inject(method = "handlePrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"), cancellable = true)
    private void silkard$skipPrecipitationGameEvent(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"), cancellable = true)
    private void silkard$receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.material.Fluid fluid, CallbackInfo ci) {
        if (!((LayeredCauldronBlock) (Object) this).isFull(state)) {
            BlockState blockstate1 = (BlockState) state.setValue(LayeredCauldronBlock.LEVEL, (Integer) state.getValue(LayeredCauldronBlock.LEVEL) + 1);
            if (!BukkitUtils.changeCauldronLevel(level, pos, blockstate1, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL)) {
                ci.cancel();
                return;
            }
            level.levelEvent(1047, pos, 0);
        }
        ci.cancel();
    }

    @Inject(method = "lowerFillLevel", at = @At("HEAD"), cancellable = true)
    private static void silkard$lowerFillLevel(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        BukkitUtils.lowerCauldronFillLevel(state, level, pos, null, CauldronLevelChangeEvent.ChangeReason.UNKNOWN);
        ci.cancel();
    }

    // Paper - Call CauldronLevelChangeEvent: the extinguish is applied in the callback below, only when the
    // level change was not cancelled. runBefore defers that callback until after entityInside returns, so the
    // direct apply here would always run first.
    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/InsideBlockEffectApplier;apply(Lnet/minecraft/world/entity/InsideBlockEffectType;)V"), cancellable = true)
    private void silkard$applyExtinguish(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean serverSide, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "lambda$entityInside$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LayeredCauldronBlock;handleEntityOnFireInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"), cancellable = true)
    private void silkard$entityOnFireInside(ServerLevel serverLevel, BlockPos pos, BlockState state, Level level, Entity entity, CallbackInfo ci) {
        BlockState target = this.precipitationType == Biome.Precipitation.SNOW
                ? (BlockState) Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, (Integer) state.getValue(LayeredCauldronBlock.LEVEL))
                : state;
        ci.cancel();
        // Paper - Fixes MC-248588: mobs only use up the water when mob griefing is on
        if (!(entity instanceof net.minecraft.world.entity.player.Player) && !serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)) {
            return;
        }
        if (BukkitUtils.lowerCauldronFillLevel(target, level, pos, entity, CauldronLevelChangeEvent.ChangeReason.EXTINGUISH)) {
            InsideBlockEffectType.EXTINGUISH.effect().accept(entity);
        }
    }
}
