package com.mohistmc.silkard.mixin.purpur.world.damagesource;

import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Add attribute clamping and armor limit config: armor above 20 counts when unlimited. */
@Mixin(CombatRules.class)
public abstract class MixinPurpurCombatRules {

    @ModifyConstant(method = {"getDamageAfterAbsorb", "getDamageAfterMagicAbsorb"}, constant = @Constant(floatValue = 20.0F), require = 2, allow = 2)
    private static float purpur$armorLimit(float original) {
        return org.purpurmc.purpur.PurpurConfig.limitArmor ? 20.0F : Float.MAX_VALUE;
    }
}
