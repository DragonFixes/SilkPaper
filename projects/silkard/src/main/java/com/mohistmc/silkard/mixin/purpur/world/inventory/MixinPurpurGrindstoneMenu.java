package com.mohistmc.silkard.mixin.purpur.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.purpurmc.purpur.PurpurConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Config for grindstones. The enchantments a grindstone keeps are configurable (curses
 * by default), and it can also strip attributes, custom model data, dye and stew effects, or the
 * name and lore.
 */
@Mixin(GrindstoneMenu.class)
public abstract class MixinPurpurGrindstoneMenu {

    @Unique
    private static final List<DataComponentType<?>> purpur$removedAttributes = List.of(
        DataComponents.CUSTOM_DATA,
        DataComponents.ATTRIBUTE_MODIFIERS,
        DataComponents.CUSTOM_MODEL_DATA,
        DataComponents.DYED_COLOR,
        DataComponents.SUSPICIOUS_STEW_EFFECTS
    );

    @WrapOperation(method = {"lambda$mergeEnchantsFrom$0", "lambda$removeNonCursesFrom$1"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z"), require = 2)
    private static boolean purpur$keptEnchantments(Holder<Enchantment> enchantment, TagKey<Enchantment> tag, Operation<Boolean> original) {
        return tag == EnchantmentTags.CURSE ? com.mohistmc.silkard.purpur.PurpurHooks.grindstoneKeeps(enchantment) : original.call(enchantment, tag);
    }

    @ModifyReturnValue(method = "removeNonCursesFrom", at = @At("RETURN"))
    private ItemStack purpur$stripComponents(ItemStack item) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        if (PurpurConfig.grindstoneRemoveAttributes) {
            item.getComponents().forEach(component -> {
                if (purpur$removedAttributes.contains(component.type())) builder.remove(component.type());
            });
        }
        if (PurpurConfig.grindstoneRemoveDisplay) {
            builder.remove(DataComponents.CUSTOM_NAME);
            builder.remove(DataComponents.LORE);
        }
        item.applyComponents(builder.build());
        return item;
    }
}
