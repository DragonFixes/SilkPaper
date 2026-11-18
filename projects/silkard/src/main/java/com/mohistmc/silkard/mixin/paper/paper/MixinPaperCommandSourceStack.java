package com.mohistmc.silkard.mixin.paper.paper;

import com.mohistmc.silkard.injected.paper.paper.PaperCommandSourceStackAccess;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.command.brigadier.PaperCommandSourceStack;
import java.util.function.BooleanSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.BaseCommandBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper's Brigadier API view of the vanilla command source and its permission helpers.
 */
@Mixin(CommandSourceStack.class)
public abstract class MixinPaperCommandSourceStack implements PaperCommandSourceStack, PaperCommandSourceStackAccess {

    @Shadow @Final private CommandSource source;
    @Shadow @Final private PermissionSet permissions;
    @Shadow @Final private boolean silent;
    @Shadow @Final private ServerLevel level;

    @Shadow public abstract @Nullable MinecraftServer getServer();
    @Shadow public abstract void sendSuccess(java.util.function.Supplier<Component> message, boolean broadcastToOps);
    @Shadow public abstract void sendFailure(Component message);

    @Unique private boolean paper$bypassSelectorPermissions;

    @Override
    public CommandSourceStack getHandle() {
        return (CommandSourceStack) (Object) this;
    }

    @Override
    public io.papermc.paper.command.brigadier.CommandSourceStack withLocation(final org.bukkit.Location location) {
        final CommandSourceStack moved = this.getHandle()
            .withLevel(((org.bukkit.craftbukkit.CraftWorld) location.getWorld()).getHandle())
            .withPosition(new net.minecraft.world.phys.Vec3(location.getX(), location.getY(), location.getZ()))
            .withRotation(new net.minecraft.world.phys.Vec2(location.getPitch(), location.getYaw()));
        return (io.papermc.paper.command.brigadier.CommandSourceStack) (Object) moved;
    }

    @Unique
    private boolean paper$forceRespectPermissionLevel() {
        return this.source == CommandSource.NULL
            || (this.source instanceof BaseCommandBlock.CloseableCommandBlockSource && this.level != null
                && this.level.paperConfig().commandBlocks.forceFollowPermLevel);
    }

    @Override
    public boolean hasPermission(final Permission permission, final String bukkitPermission) {
        // Bukkit permissions are checked lazily, so custom permission plugins are only asked when needed.
        final BooleanSupplier hasBukkitPerm = () -> this.source == CommandSource.NULL || this.getHandle().getBukkitSender().hasPermission(bukkitPermission);
        // Without a server (command function loading) the vanilla level system decides.
        final MinecraftServer server = this.getServer();
        if (server == null || !server.silkard$server().ignoreVanillaPermissions) {
            final boolean hasPermLevel = this.permissions.hasPermission(permission);
            if (this.paper$forceRespectPermissionLevel()) {
                return hasPermLevel && hasBukkitPerm.getAsBoolean();
            }
            return hasPermLevel || hasBukkitPerm.getAsBoolean();
        }
        return hasBukkitPerm.getAsBoolean();
    }

    @Override
    public boolean testPermission(final Permission permission, final String bukkitPermission) {
        if (this.hasPermission(permission, bukkitPermission)) {
            return true;
        }
        final MinecraftServer server = this.getServer();
        if (server != null) {
            final net.kyori.adventure.text.Component permissionMessage = server.silkard$server().permissionMessage();
            if (!permissionMessage.equals(net.kyori.adventure.text.Component.empty())) {
                this.sendFailure(PaperAdventure.asVanilla(permissionMessage.replaceText(
                    net.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("<permission>").replacement(bukkitPermission).build())));
            }
        }
        return false;
    }

    @Override
    public boolean paper$bypassSelectorPermissions() {
        return this.paper$bypassSelectorPermissions;
    }

    @Override
    public void paper$bypassSelectorPermissions(final boolean bypass) {
        this.paper$bypassSelectorPermissions = bypass;
    }

    @Override
    public void sendFailure(final Component message, final boolean withStyle) {
        if (withStyle) {
            this.sendFailure(message);
            return;
        }
        if (this.source.acceptsFailure() && !this.silent) {
            this.source.sendSystemMessage(message);
        }
    }

    @Override
    public void sendSuccess(final net.kyori.adventure.text.@Nullable Component message, final boolean broadcastToOps) {
        if (message == null) {
            return;
        }
        this.sendSuccess(() -> PaperAdventure.asVanilla(message), broadcastToOps);
    }
}
