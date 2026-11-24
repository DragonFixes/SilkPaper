package com.mohistmc.silkard.mixin.purpur.world.level.block.entity;

import com.mohistmc.silkard.purpur.PurpurLapisHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Purpur - Enchantment Table Persists Lapis. The table stores the lapis left in it, saved as
 * {@code Purpur.Lapis}, and drops it when broken.
 */
@Mixin(EnchantingTableBlockEntity.class)
public abstract class MixinPurpurEnchantingTableBlockEntity extends BlockEntity implements PurpurLapisHolder {

    @Unique private int purpur$lapis;

    private MixinPurpurEnchantingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int purpur$getLapis() {
        return this.purpur$lapis;
    }

    @Override
    public void purpur$setLapis(int lapis) {
        this.purpur$lapis = lapis;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void purpur$saveLapis(ValueOutput output, CallbackInfo ci) {
        output.putInt("Purpur.Lapis", this.purpur$lapis);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void purpur$loadLapis(ValueInput input, CallbackInfo ci) {
        this.purpur$lapis = input.getIntOr("Purpur.Lapis", 0);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && this.level.purpurConfig().enchantmentTableLapisPersists && this.purpur$lapis > 0) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.LAPIS_LAZULI, this.purpur$lapis));
        }
    }
}
