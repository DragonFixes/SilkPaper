package com.mohistmc.silkard.mixin.vanilla.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * CraftBukkit/Paper - an empty bed that is destroyed on use (outside the Overworld) reports the
 * explosion as a sleeping problem, so PlayerBedEnterEvent and PlayerBedFailEnterEvent can stop it;
 * an occupied bed explodes immediately. Every failed sleep fires PlayerBedFailEnterEvent, whose
 * message and explosion flag replace vanilla's.
 */
@Mixin(AbstractBedBlock.class)
public abstract class MixinAbstractBedBlockEvents {

    @Shadow protected abstract InteractionResult destroyOnUse(BlockState state, Level level, BlockPos pos, Player player);

    @WrapOperation(method = "useWithoutItem", require = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;destroyOnUse()Z", ordinal = 0))
    private boolean silkard$explodeOnlyWhenOccupied(BedRule rule, Operation<Boolean> original, @Local(argsOnly = true) BlockState state) {
        return original.call(rule) && state.getValue(AbstractBedBlock.OCCUPIED);
    }

    @WrapOperation(method = "useWithoutItem", require = 1,
        at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;ifLeft(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"))
    private Either<Player.BedSleepingProblem, Unit> silkard$bedFailEvent(Either<Player.BedSleepingProblem, Unit> result, Consumer<Player.BedSleepingProblem> vanillaMessage,
                                                                     Operation<Either<Player.BedSleepingProblem, Unit>> original,
                                                                     @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Level level,
                                                                     @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) Player player,
                                                                     @Local BedRule bedRule) {
        return original.call(result, (Consumer<Player.BedSleepingProblem>) problem -> {
            if (problem == null) {
                return;
            }
            PlayerBedFailEnterEvent event = CraftEventFactory.callPlayerBedFailEnterEvent(player, pos, bedRule, problem);
            if (event.isCancelled()) {
                return;
            }
            if (event.getWillExplode()) {
                this.destroyOnUse(state, level, pos, player);
            }
            net.kyori.adventure.text.Component message = event.getMessage();
            if (message != null) {
                player.sendOverlayMessage(io.papermc.paper.adventure.PaperAdventure.asVanilla(message));
            }
        });
    }
}
