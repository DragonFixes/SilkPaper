package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrogspawnBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Purpur - Breedable mobs can have multiple babies. The tadpole count comes from the frog
 * breeding offspring range and each tadpole rolls the breeding chance; the first failed roll ends
 * the hatching.
 */
@Mixin(FrogspawnBlock.class)
public abstract class MixinPurpurFrogspawnBlock {

    @WrapOperation(method = "spawnTadpoles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(II)I", ordinal = 0))
    private int purpur$tadpoleAmount(RandomSource random, int min, int max, Operation<Integer> original, ServerLevel level, BlockPos pos, RandomSource random2) {
        return original.call(random, level.purpurConfig().frogBreedingMinOffspring, level.purpurConfig().frogBreedingMaxOffspring + 1);
    }

    @WrapOperation(method = "spawnTadpoles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"))
    private Entity purpur$breedingChance(EntityType<?> type, Level world, EntitySpawnReason reason, Operation<Entity> original,
                                         ServerLevel level, BlockPos pos, RandomSource random, @Share("stopped") LocalBooleanRef stopped) {
        if (stopped.get()) return null;
        double chance = level.purpurConfig().frogBreedingChance;
        if (chance <= 0.0D || chance < random.nextDouble()) {
            stopped.set(true);
            return null;
        }
        return original.call(type, world, reason);
    }
}
