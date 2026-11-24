package com.mohistmc.silkard.mixin.purpur.world.level.block.piston;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Configurable piston push limit. */
@Mixin(PistonStructureResolver.class)
public abstract class MixinPurpurPistonStructureResolver {

    @Shadow @Final private Level level;

    @ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12), require = 3)
    private int purpur$pushLimit(int original) {
        return this.level.purpurConfig().pistonBlockPushLimit;
    }
}
