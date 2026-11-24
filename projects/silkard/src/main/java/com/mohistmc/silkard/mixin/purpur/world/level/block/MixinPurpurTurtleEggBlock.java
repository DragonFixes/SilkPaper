package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Purpur - turtle egg block options: what can break eggs, crack chance, griefing override. */
@Mixin(TurtleEggBlock.class)
public abstract class MixinPurpurTurtleEggBlock {

    @Inject(method = "canDestroyEgg", at = @At("HEAD"), cancellable = true)
    private void purpur$breakFrom(ServerLevel level, BlockPos pos, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if ((level.purpurConfig().turtleEggsBreakFromExpOrbs && entity instanceof ExperienceOrb)
            || (level.purpurConfig().turtleEggsBreakFromItems && entity instanceof ItemEntity)
            || (level.purpurConfig().turtleEggsBreakFromMinecarts && entity instanceof AbstractMinecart)) {
            cir.setReturnValue(true);
        }
    }

    // Option to disable turtle egg trampling with feather falling: a player whose feather falling
    // level covers the fall does not trample, in the same way spawn protection stops trampling.
    @ModifyExpressionValue(method = "canDestroyEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isUnderSpawnProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean purpur$featherFalling(boolean protectedPos, ServerLevel level, BlockPos pos, Entity entity) {
        return protectedPos || purpur$featherFallingCovers(level, entity);
    }

    @Unique
    private static boolean purpur$featherFallingCovers(ServerLevel level, Entity entity) {
        if (!level.purpurConfig().turtleEggsTramplingFeatherFalling || !(entity instanceof LivingEntity living)) return false;
        ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
        return !boots.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING), boots) >= (int) entity.fallDistance;
    }

    @WrapOperation(method = "canDestroyEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object purpur$mobGriefingOverride(GameRules rules, GameRule<?> rule, Operation<Object> original, ServerLevel level, BlockPos pos, Entity entity) {
        Boolean override = level.purpurConfig().turtleEggsMobGriefingOverride;
        return override != null ? override : original.call(rules, rule);
    }

    // Turtle eggs random tick crack chance; 500 keeps the environment attribute's chance.
    @ModifyExpressionValue(method = "shouldUpdateHatchLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;"))
    private Object purpur$crackChance(Object chance, Level level, BlockPos pos) {
        int configured = level.purpurConfig().turtleEggsRandomTickCrackChance;
        return configured == 500 ? chance : (Object) (float) configured;
    }
}
