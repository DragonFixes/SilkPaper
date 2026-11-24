package com.mohistmc.silkard.mixin.paper.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface MixinPaperLivingEntityDeathInvoker {

    @Invoker("dropExperience")
    void silkard$dropExperience(ServerLevel level, @Nullable Entity killer);
}
