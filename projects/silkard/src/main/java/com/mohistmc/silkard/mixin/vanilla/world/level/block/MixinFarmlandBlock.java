package com.mohistmc.silkard.mixin.vanilla.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mgazul
 * @date 2026/10/24
 */
@Mixin(FarmlandBlock.class)
public class MixinFarmlandBlock {

    // CraftBukkit start - MoistureChangeEvent
    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean silkard$moistureChange(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleMoistureChangeEvent(level, pos, state, flags);
    }

    // CraftBukkit - EntityChangeBlockEvent when turning to dirt
    @Redirect(method = "turnToBaseBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean silkard$turnToDirt(Level level, BlockPos pos, BlockState state, Entity sourceEntity) {
        if (sourceEntity == null) {
            return level.setBlockAndUpdate(pos, state);
        }
        return CraftEventFactory.callEntityChangeBlockEvent(sourceEntity, pos, state) && level.setBlockAndUpdate(pos, state);
    }


    // CraftBukkit end
}
