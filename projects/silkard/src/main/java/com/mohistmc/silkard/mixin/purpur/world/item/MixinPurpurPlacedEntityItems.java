package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PostSpawnProcessor;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Apply display names from item forms of entities to entities. With
 * persistent-droppable-entity-display-names off, armor stands and hanging entities placed from a
 * named item get no name. The name is cleared after the item's components are applied, so it
 * cannot come back from them. Armor stands can also show their name.
 */
@Mixin({ArmorStandItem.class, HangingEntityItem.class})
public abstract class MixinPurpurPlacedEntityItems {

    @ModifyExpressionValue(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;createDefaultStackConfig(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/PostSpawnProcessor;"))
    private <T extends Entity> PostSpawnProcessor<T> purpur$displayNames(PostSpawnProcessor<T> config, @Local(argsOnly = true) UseOnContext context) {
        var purpur = context.getLevel().purpurConfig();
        boolean armorStand = (Object) this instanceof ArmorStandItem;
        return config.andThen(entity -> {
            if (!purpur.persistentDroppableEntityDisplayNames) entity.setCustomName(null);
            if (armorStand && purpur.armorstandSetNameVisible && entity.getCustomName() != null) entity.setCustomNameVisible(true);
        });
    }
}
