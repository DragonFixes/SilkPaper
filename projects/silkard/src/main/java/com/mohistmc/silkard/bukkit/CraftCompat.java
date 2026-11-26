package com.mohistmc.silkard.bukkit;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.event.block.BlockBrushEvent;
import org.bukkit.event.entity.EntityTargetBlockEvent;
import org.bukkit.event.player.PlayerCustomClickEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * CraftBukkit conversions that Silkard's mixins use under their CraftBukkit names, implemented
 * on top of Paper's CraftBukkit.
 */
public final class CraftCompat {

    private CraftCompat() {
    }

    /**
     * A Bukkit view backed by {@code stack}; changes through it write to the vanilla stack.
     * Paper's {@link CraftItemStack#asBukkitMirror} always builds a {@link CraftItemStack}.
     */
    public static CraftItemStack craftMirror(ItemStack stack) {
        return (CraftItemStack) CraftItemStack.asBukkitMirror(stack);
    }

    /** A Bukkit stack built from a recipe result template; templates are immutable, so this is a fresh stack. */
    public static CraftItemStack craftMirror(ItemStackTemplate template) {
        return (CraftItemStack) CraftItemStack.asBukkitMirror(template.create());
    }

    /**
     * Writes a captured block state back to the world. Captured tree/placement states are set
     * with their captured flags and no extra physics, so neighbours update the way the vanilla
     * placement would have; any other state is a plain forced update.
     */
    public static void placeCaptured(org.bukkit.block.BlockState state) {
        if (state instanceof CapturedBlockState captured) {
            captured.place(captured.getFlags());
        } else {
            state.update(true);
        }
    }

    /**
     * Runs a console or RCON command line: input goes to a running conversation first, then to
     * {@code CraftServer#dispatchCommand}. Exceptions are logged rather than thrown so one bad
     * command cannot stop the console loop.
     */
    public static boolean dispatchServerCommand(org.bukkit.craftbukkit.CraftServer server, org.bukkit.command.CommandSender sender, String commandLine) {
        if (sender instanceof org.bukkit.conversations.Conversable conversable && conversable.isConversing()) {
            conversable.acceptConversationInput(commandLine);
            return true;
        }
        try {
            if (server.dispatchCommand(sender, commandLine)) {
                return true;
            }
            // Nothing in the tree matched. Paper's console runs every line through the vanilla
            // dispatcher, which reports the unknown command (and fires UnknownCommandEvent) itself.
            server.getServer().getCommands().performPrefixedCommand(
                org.bukkit.craftbukkit.command.VanillaCommandWrapper.getListener(sender), commandLine);
            return false;
        } catch (Exception ex) {
            server.getLogger().log(java.util.logging.Level.WARNING, "Unexpected exception while parsing console command \"" + commandLine + '"', ex);
            return false;
        }
    }

    // Spigot CraftEventFactory events that Paper's CraftEventFactory does not provide.

    /**
     * EntityDeathEvent over a list of Bukkit drops that the caller collected, dropping whatever
     * the event leaves in that list at the victim's location.
     */
    public static org.bukkit.event.entity.EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, net.minecraft.world.damagesource.DamageSource damageSource, java.util.List<org.bukkit.inventory.ItemStack> drops) {
        org.bukkit.craftbukkit.entity.CraftLivingEntity entity = (org.bukkit.craftbukkit.entity.CraftLivingEntity) victim.getBukkitEntity();
        org.bukkit.craftbukkit.CraftWorld world = (org.bukkit.craftbukkit.CraftWorld) entity.getWorld();
        org.bukkit.event.entity.EntityDeathEvent event = new org.bukkit.event.entity.EntityDeathEvent(
            entity, new org.bukkit.craftbukkit.damage.CraftDamageSource(damageSource), drops, victim.getExpReward(world.getHandle(), damageSource.getEntity()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return event;
        }

        ((com.mohistmc.silkard.injected.world.entity.ContextLivingEntity) victim).silkard$expToDrop(event.getDroppedExp());
        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType().isAir() || stack.getAmount() == 0) {
                continue;
            }
            world.dropItem(entity.getLocation(), stack);
        }
        return event;
    }

    public static PlayerCustomClickEvent callPlayerCustomClickEvent(Identifier identifier, Optional<Tag> payload, ServerPlayer player) {
        NamespacedKey id = CraftNamespacedKey.fromMinecraft(identifier);
        JsonElement data = payload.map(nbt -> NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)).orElse(null);
        PlayerCustomClickEvent event = new PlayerCustomClickEvent(player.getBukkitEntity(), id, data);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @SuppressWarnings("deprecation") // the String motd constructor is what legacy (pre-1.7) pings carry
    public static ServerListPingEvent callServerListPingEvent(SocketAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent("", ((InetSocketAddress) address).getAddress(), motd, numPlayers, maxPlayers);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handlePlayerRecipeListUpdateEvent(Player who, Identifier recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((org.bukkit.entity.Player) who.getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static EntityTargetBlockEvent callEntityTargetBlockEvent(Entity entity, ServerLevel level, BlockPos target) {
        EntityTargetBlockEvent event = new EntityTargetBlockEvent(entity.getBukkitEntity(), target == null ? null : CraftBlock.at(level, target));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(Entity entity, Player player, InteractionHand hand) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (org.bukkit.entity.Player) player.getBukkitEntity(), CraftEquipmentSlot.getHand(hand));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * CrafterCraftEvent with Spigot's remaining-items list, so plugins can change what the
     * crafter ejects besides the result.
     */
    public static org.bukkit.event.block.CrafterCraftEvent callCrafterCraftEvent(BlockPos pos, Level level, ItemStack result, java.util.List<ItemStack> remainingItems, net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> holder) {
        java.util.List<org.bukkit.inventory.ItemStack> remaining = new java.util.ArrayList<>(remainingItems.size());
        for (ItemStack stack : remainingItems) {
            remaining.add(CraftItemStack.asBukkitCopy(stack));
        }
        org.bukkit.event.block.CrafterCraftEvent event = new org.bukkit.event.block.CrafterCraftEvent(
            CraftBlock.at(level, pos), (org.bukkit.inventory.CraftingRecipe) holder.toBukkitRecipe(), CraftItemStack.asBukkitMirror(result), remaining);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Fires PlayerShearEntityEvent before a player shears {@code sheared}; returns whether the
     * shearing may go ahead. Vanilla rolls the shear loot inside the shear method itself, so the
     * event carries no drops.
     */
    public static boolean handlePlayerShear(Player player, Entity sheared, ItemStack shears, InteractionHand hand) {
        // TODO(silkard-paper): roll the shear loot up front so PlayerShearEntityEvent#getDrops/setDrops reach the dropped items.
        return !org.bukkit.craftbukkit.event.CraftEventFactory.handlePlayerShearEntityEvent(player, sheared, shears, hand, new java.util.ArrayList<>()).isCancelled();
    }

    public static BlockBrushEvent callBlockBrushEvent(Level level, BlockPos pos, BlockState newData, int flags, Player entity) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) entity.getBukkitEntity();
        CraftBlock block = CraftBlock.at(level, pos);
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setBlock(newData);
        state.setFlags(flags);
        BlockBrushEvent event = new BlockBrushEvent(block, state, player);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
