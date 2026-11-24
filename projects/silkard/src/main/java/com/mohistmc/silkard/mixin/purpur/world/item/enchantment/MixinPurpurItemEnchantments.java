package com.mohistmc.silkard.mixin.purpur.world.item.enchantment;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Purpur - Add toggle for enchant level clamping. With clamping off, enchantment levels up to
 * 32767 are kept instead of being rejected above 255. The setting is read before vanilla
 * bootstrap ({@code PaperBootstrap.loadEarlyPurpurSettings}), so the level codec built in the
 * static initializer follows it too.
 */
@Mixin(ItemEnchantments.class)
public abstract class MixinPurpurItemEnchantments {

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 255), require = 1)
    private static int purpur$codecMaxLevel(int original) {
        return org.purpurmc.purpur.PurpurConfig.clampEnchantLevels ? 255 : 32767;
    }

    @ModifyConstant(method = "<init>(Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;)V", constant = @Constant(intValue = 255), require = 1)
    private int purpur$maxLevel(int original) {
        return org.purpurmc.purpur.PurpurConfig.clampEnchantLevels ? 255 : 32767;
    }
}
