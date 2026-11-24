package com.mohistmc.silkard.mixin.purpur.world.item.enchantment;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Purpur - Add toggle for enchant level clamping. With clamping off, enchantment levels up to
 * 32767 are kept instead of being rejected above 255. The level codec is built during bootstrap,
 * before the config loads, so it keeps vanilla's limit, as in Purpur.
 */
@Mixin(ItemEnchantments.class)
public abstract class MixinPurpurItemEnchantments {

    @ModifyConstant(method = "<init>(Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;)V", constant = @Constant(intValue = 255))
    private int purpur$maxLevel(int original) {
        return org.purpurmc.purpur.PurpurConfig.clampEnchantLevels ? 255 : 32767;
    }
}
