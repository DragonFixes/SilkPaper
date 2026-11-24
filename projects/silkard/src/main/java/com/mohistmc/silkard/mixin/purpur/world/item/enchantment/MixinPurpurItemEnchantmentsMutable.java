package com.mohistmc.silkard.mixin.purpur.world.item.enchantment;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Purpur - Add toggle for enchant level clamping, for levels set or upgraded on an item. */
@Mixin(ItemEnchantments.Mutable.class)
public abstract class MixinPurpurItemEnchantmentsMutable {

    @ModifyConstant(method = {"set", "upgrade"}, constant = @Constant(intValue = 255), require = 2)
    private int purpur$maxLevel(int original) {
        return org.purpurmc.purpur.PurpurConfig.clampEnchantLevels ? 255 : 32767;
    }
}
