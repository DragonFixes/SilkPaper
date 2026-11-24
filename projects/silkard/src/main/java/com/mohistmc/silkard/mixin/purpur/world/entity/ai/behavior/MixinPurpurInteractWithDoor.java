package com.mohistmc.silkard.mixin.purpur.world.entity.ai.behavior;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Option to make doors require redstone: mobs neither open nor close doors that need
 * redstone, so those doors are not treated as doors they can use.
 */
@Mixin(InteractWithDoor.class)
public abstract class MixinPurpurInteractWithDoor {

    @WrapOperation(method = {"lambda$create$2", "closeDoorsThatIHaveOpenedOrPassedThrough"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;Ljava/util/function/Predicate;)Z"), require = 3)
    private static boolean purpur$doorRequiresRedstone(BlockState state, TagKey<Block> tag, Predicate<?> predicate, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level) {
        return original.call(state, tag, predicate) && !level.purpurConfig().doorRequiresRedstone.contains(state.getBlock());
    }
}
