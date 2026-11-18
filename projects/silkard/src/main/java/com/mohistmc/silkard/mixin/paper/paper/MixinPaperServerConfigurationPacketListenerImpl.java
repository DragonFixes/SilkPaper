package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperConfigurationListenerAccess;
import io.papermc.paper.connection.PaperConfigurationTask;
import io.papermc.paper.connection.PaperPlayerConfigurationConnection;
import java.util.Queue;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives each configuration listener its Paper {@link PaperPlayerConfigurationConnection} and queues
 * the task that fires {@code AsyncPlayerConnectionConfigureEvent} ahead of spawning the player.
 */
@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class MixinPaperServerConfigurationPacketListenerImpl implements PaperConfigurationListenerAccess {

    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;

    @Unique private PaperPlayerConfigurationConnection paper$paperConnection;

    // Created after the constructor body, since the connection view reads the listener's connection field.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void paper$createConnection(final CallbackInfo ci) {
        this.paper$paperConnection = new PaperPlayerConfigurationConnection((ServerConfigurationPacketListenerImpl) (Object) this);
    }

    @Inject(method = "startConfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;returnToWorld()V"))
    private void paper$queueConfigureEvent(final CallbackInfo ci) {
        this.configurationTasks.add(new PaperConfigurationTask((ServerConfigurationPacketListenerImpl) (Object) this));
    }

    @Override
    public PaperPlayerConfigurationConnection paper$paperConnection() {
        return this.paper$paperConnection;
    }
}
