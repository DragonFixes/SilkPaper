package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.paper.server.DeathCapture;
import com.mohistmc.silkard.paper.server.DefaultDrop;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CraftBukkit - Capture drops for death event. While a living entity dies, items it spawns are
 * captured instead; each capture spawns its item entity itself, so dropping it later is never
 * captured again.
 */
@Mixin(Entity.class)
public abstract class MixinPaperEntityDeathDrops {

    @Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void silkard$captureDeathDrop(ServerLevel level, ItemStack itemStack, Vec3 offset, CallbackInfoReturnable<ItemEntity> cir) {
        if (itemStack.isEmpty() || !((Object) this instanceof DeathCapture capture)) {
            return;
        }
        List<DefaultDrop> drops = capture.silkard$deathDropItems();
        if (drops == null) {
            return;
        }
        Entity self = (Entity) (Object) this;
        drops.add(new DefaultDrop(itemStack, dropStack -> { // Paper - Restore vanilla drops behavior
            ItemEntity dropEntity = new ItemEntity(level, self.getX() + offset.x, self.getY() + offset.y, self.getZ() + offset.z, dropStack);
            dropEntity.setDefaultPickUpDelay();
            level.addFreshEntity(dropEntity);
        }));
        cir.setReturnValue(null);
    }
}
