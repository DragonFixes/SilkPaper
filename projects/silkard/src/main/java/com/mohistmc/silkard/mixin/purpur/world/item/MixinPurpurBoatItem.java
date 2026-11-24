package com.mohistmc.silkard.mixin.purpur.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Purpur - Apply display names from item forms of entities to entities, for boats. */
@Mixin(BoatItem.class)
public abstract class MixinPurpurBoatItem {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BoatItem;getBoat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/HitResult;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;"))
    private AbstractBoat purpur$displayName(AbstractBoat boat, @Local(argsOnly = true) Level level) {
        if (boat != null && !level.purpurConfig().persistentDroppableEntityDisplayNames) boat.setCustomName(null);
        return boat;
    }
}
