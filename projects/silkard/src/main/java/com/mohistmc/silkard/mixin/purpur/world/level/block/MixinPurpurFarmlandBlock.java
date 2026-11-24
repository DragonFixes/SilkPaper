package com.mohistmc.silkard.mixin.purpur.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Farmland trampling as in Paper and Purpur. Fall damage is applied first, so trample rules and
 * events never change it. Trampling rolls once, fires the interact event, then the Purpur rules.
 * The entity change event fires inside {@code turnToBaseBlock}.
 */
@Mixin(FarmlandBlock.class)
public abstract class MixinPurpurFarmlandBlock extends Block {

    private MixinPurpurFarmlandBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author SilkPaper
     * @reason CraftBukkit trample events, Purpur trample height, griefing override, trampling options
     */
    @Overwrite
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance); // CraftBukkit - game rules and events do not affect fall damage
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity)) return;
        double trampleHeight = serverLevel.purpurConfig().farmlandTrampleHeight;
        if (!(trampleHeight >= 0D ? fallDistance >= trampleHeight : level.getRandom().nextFloat() < fallDistance - 0.5)) return;

        boolean canTrample = entity instanceof Player player
            ? !level.getServer().isUnderSpawnProtection(serverLevel, pos, player)
            : serverLevel.getGameRules().get(GameRules.MOB_GRIEFING, serverLevel.purpurConfig().farmlandMobGriefingOverride);
        if (!canTrample || !(entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F)) return;

        // CraftBukkit start - Interact soil
        org.bukkit.event.Cancellable cancellable;
        if (entity instanceof Player player) {
            cancellable = CraftEventFactory.callPlayerInteractEvent(player, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
        } else {
            cancellable = new org.bukkit.event.entity.EntityInteractEvent(entity.getBukkitEntity(), org.bukkit.craftbukkit.block.CraftBlock.at(level, pos));
            level.getCraftServer().getPluginManager().callEvent((org.bukkit.event.entity.EntityInteractEvent) cancellable);
        }
        if (cancellable.isCancelled()) return;
        // CraftBukkit end

        if (level.purpurConfig().farmlandTramplingDisabled) return;
        if (level.purpurConfig().farmlandTramplingOnlyPlayers && !(entity instanceof Player)) return;
        if (level.purpurConfig().farmlandAlpha) {
            Block below = level.getBlockState(pos.below()).getBlock();
            if (below instanceof FenceBlock || below instanceof WallBlock) return;
        }
        if (level.purpurConfig().farmlandTramplingFeatherFalling) {
            ItemStack boots = ((LivingEntity) entity).getItemBySlot(EquipmentSlot.FEET);
            if (boots != ItemStack.EMPTY && EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING), boots) >= (int) entity.fallDistance) {
                return;
            }
        }
        ((FarmlandBlock) (Object) this).turnToBaseBlock(entity, state, level, pos);
    }

    // Purpur - Allow soil to moisten from water directly under it
    @ModifyReturnValue(method = "isNearWater", at = @At("RETURN"))
    private static boolean purpur$moistFromBelow(boolean nearWater, LevelReader level, BlockPos pos) {
        return nearWater || (level instanceof Level world && world.purpurConfig().farmlandGetsMoistFromBelow
            && level.getFluidState(pos.relative(Direction.DOWN)).is(FluidTags.WATER));
    }
}
