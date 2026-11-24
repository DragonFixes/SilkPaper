package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Add allow water in end world option: ice in the End melts to air unless allowed. */
@Mixin(IceBlock.class)
public abstract class MixinPurpurIceBlock {

    private static final String WATER_EVAPORATES = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;";

    @ModifyExpressionValue(method = "playerDestroy", at = @At(value = "INVOKE", target = WATER_EVAPORATES), require = 1)
    private Object purpur$evaporatesOnBreak(Object evaporates, @Local(argsOnly = true) ServerLevel level) {
        return purpur$evaporates(evaporates, level);
    }

    @ModifyExpressionValue(method = "melt", at = @At(value = "INVOKE", target = WATER_EVAPORATES), require = 1)
    private Object purpur$evaporatesOnMelt(Object evaporates, @Local(argsOnly = true) Level level) {
        return purpur$evaporates(evaporates, level);
    }

    @Unique
    private static Object purpur$evaporates(Object evaporates, Level level) {
        if (!org.purpurmc.purpur.PurpurConfig.allowWaterPlacementInTheEnd && level.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) {
            return Boolean.TRUE;
        }
        return evaporates;
    }
}
