package com.mohistmc.silkard.mixin.paper.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper's Brigadier command API hooks on data-pack resource loading: every new {@link Commands}
 * tree becomes the dispatcher behind {@code PaperCommands}, Paper's own commands are registered
 * into it, and bootstrap plugins get the {@code COMMANDS} lifecycle event.
 * <p>
 * Paper also passes a {@code modern} flag to the {@code Commands} constructor so the
 * {@code minecraft:} aliases are node copies rather than redirects; vanilla's constructor is kept
 * here, so the aliases keep CraftBukkit's behaviour.
 */
@Mixin(ReloadableServerResources.class)
public abstract class MixinPaperReloadableServerResources {

    /** The first resource load is the server's initial one; every later load is a reload. */
    @Unique
    private static boolean silkard$initialLoadDone;

    @Shadow
    @Final
    private Commands commands;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void paper$brigadierCommandApi(ReloadableServerRegistries.LoadResult fullRegistries, FeatureFlagSet enabledFeatures,
                                           Commands.CommandSelection commandSelection, List<Registry.PendingTags<?>> postponedTags,
                                           PermissionSet functionCompilationPermissions,
                                           List<DataComponentInitializers.PendingComponents<?>> newComponents, CallbackInfo ci) {
        io.papermc.paper.command.brigadier.PaperCommands.INSTANCE.setDispatcher(
            this.commands, CommandBuildContext.simple(fullRegistries.lookupWithUpdatedTags(), enabledFeatures));
        io.papermc.paper.command.PaperCommands.registerCommands();

        final ReloadableRegistrarEvent.Cause cause = silkard$initialLoadDone
            ? ReloadableRegistrarEvent.Cause.RELOAD
            : ReloadableRegistrarEvent.Cause.INITIAL;
        silkard$initialLoadDone = true;
        LifecycleEventRunner.INSTANCE.callReloadableRegistrarEvent(
            LifecycleEvents.COMMANDS,
            io.papermc.paper.command.brigadier.PaperCommands.INSTANCE,
            BootstrapContext.class,
            cause);
    }
}
