package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.bukkit.InventoryCloseReasonHolder;
import com.mohistmc.silkard.injected.server.level.ContextServerPlayer;
import com.mohistmc.silkard.paper.server.DeathCapture;
import com.mohistmc.silkard.paper.server.DefaultDrop;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * CraftBukkit's PlayerDeathEvent with Paper's additions: the inventory and player loot table
 * drops are shown to the event before anything drops, plugins can cancel the death, change the
 * death message and screen, keep items, keep the inventory or level, and skip the experience.
 *
 * <p>The priority is below the default so other mods' injections into {@code die}, such as
 * Fabric API's kill and death events, apply on top of the overwrite.
 */
@Mixin(value = ServerPlayer.class, priority = 500)
public abstract class MixinPaperServerPlayerDeath extends Player {

    @Shadow public net.minecraft.server.network.ServerGamePacketListenerImpl connection;
    @Shadow @Final private MinecraftServer server;

    @Shadow private void tellNeutralMobsThatIDied() {}
    @Shadow public abstract ServerLevel level();

    private MixinPaperServerPlayerDeath(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    /**
     * @author SilkPaper
     * @reason CraftBukkit PlayerDeathEvent; Paper restored drops, cancellable deaths, items to keep
     */
    @Overwrite
    public void die(DamageSource source) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        boolean showDeathMessage = this.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
        if (self.isRemoved()) {
            return;
        }
        // Paper - Restore vanilla drops behavior: the inventory drops without firing drop events
        Inventory inventory = this.getInventory();
        List<DefaultDrop> loot = new ArrayList<>(inventory.getContainerSize());
        boolean keepInventory = this.level().getGameRules().get(GameRules.KEEP_INVENTORY) || this.isSpectator();
        if (!keepInventory) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (!item.isEmpty() && !EnchantmentHelper.has(item, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    loot.add(new DefaultDrop(item, stack -> {
                        net.minecraft.world.entity.item.ItemEntity dropped = self.createItemStackToDrop(stack, org.dreeam.leaf.config.modules.gameplay.DeathItemDropKnockback.dropAround, false);
                        if (dropped != null) self.level().addFreshEntity(dropped);
                    }));
                }
            }
        }
        if (!this.isSpectator() && this.shouldDropLoot(this.level())) { // Paper - no player loot tables when mob loot is off
            // SPIGOT-5071: player loot tables drop despite keepInventory (SPIGOT-5195)
            DeathCapture capture = (DeathCapture) this;
            capture.silkard$setDeathDropItems(new ArrayList<>());
            this.dropFromLootTable(this.level(), source, this.lastHurtByPlayerMemoryTime > 0);
            List<DefaultDrop> lootTableDrops = capture.silkard$deathDropItems();
            if (lootTableDrops != null) loot.addAll(lootTableDrops);
            capture.silkard$setDeathDropItems(null);
        }

        Component defaultMessage = this.getCombatTracker().getDeathMessage();
        ((ContextServerPlayer) self).silkard$keepLevel(keepInventory); // SPIGOT-2222: pre-set keepLevel
        PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(self, source, loot, io.papermc.paper.adventure.PaperAdventure.asAdventure(defaultMessage), showDeathMessage, keepInventory);
        // Paper start - cancellable death event
        if (event.isCancelled()) {
            if (this.getHealth() <= 0) {
                this.setHealth((float) event.getReviveHealth());
            }
            return;
        }
        this.gameEvent(GameEvent.ENTITY_DIE);
        // Paper end

        // SPIGOT-943 - only close a container that is open
        if (this.containerMenu != this.inventoryMenu) {
            ((InventoryCloseReasonHolder) self).silkard$closeContainer(InventoryCloseEvent.Reason.DEATH); // Paper - Inventory close reason
        }

        net.kyori.adventure.text.Component apiDeathMessage = event.deathMessage() != null ? event.deathMessage() : net.kyori.adventure.text.Component.empty();
        Component deathScreenMessage = io.papermc.paper.adventure.PaperAdventure.asVanilla(event.deathScreenMessageOverride() != null ? event.deathScreenMessageOverride() : apiDeathMessage);
        if (apiDeathMessage != net.kyori.adventure.text.Component.empty() && event.getShowDeathMessages()) {
            Component deathMessage = io.papermc.paper.adventure.PaperAdventure.asVanilla(apiDeathMessage);
            this.silkard$sendCombatKillPacket(true, deathScreenMessage);
            Team team = this.getTeam();
            if (org.purpurmc.purpur.PurpurConfig.deathMessageOnlyBroadcastToAffectedPlayer) {
                self.sendSystemMessage(deathMessage); // Purpur - Configurable broadcast settings
            } else if (team == null || team.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
                this.server.getPlayerList().broadcastSystemMessage(deathMessage, false);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastSystemToTeam(self, deathMessage);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastSystemToAllExceptTeam(self, deathMessage);
            }
        } else {
            this.silkard$sendCombatKillPacket(event.getShowDeathMessages(), deathScreenMessage);
        }

        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        // SPIGOT-5478 must be called manually now
        if (event.shouldDropExperience()) {
            ((MixinPaperLivingEntityDeathInvoker) this).silkard$dropExperience(this.level(), source.getEntity()); // Paper - tie to event
        }
        // The inventory is cleared after the event, so plugins see its exact state in the event.
        if (!event.getKeepInventory()) {
            // Paper start - PlayerDeathEvent#getItemsToKeep
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (!silkard$keepsItem(event, inventory.getItem(slot))) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
            // items to keep that were not in the inventory at death, added by plugins
            for (org.bukkit.inventory.ItemStack itemStack : event.getItemsToKeep()) {
                event.getEntity().getInventory().addItem(itemStack);
            }
            // Paper end - PlayerDeathEvent#getItemsToKeep
        }

        self.setCamera(self); // Remove spectated target
        this.level().getCraftServer().getScoreboardManager().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, self, ScoreAccess::increment); // CraftBukkit - per-player scoreboards
        LivingEntity killer = this.getKillCredit();
        if (killer != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(killer.getType()));
            killer.awardKillScore(self, source);
            this.createWitherRose(killer);
        }

        this.level().broadcastEntityEvent(self, EntityEvent.DEATH);
        this.awardStat(Stats.DEATHS);
        self.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        self.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
        this.connection.markClientUnloadedAfterDeath();
    }

    /** Paper - PlayerDeathEvent#getItemsToKeep: whether this item stays, using up its entry. */
    @Unique
    private static boolean silkard$keepsItem(PlayerDeathEvent event, ItemStack item) {
        List<org.bukkit.inventory.ItemStack> itemsToKeep = event.getItemsToKeep();
        if (EnchantmentHelper.has(item, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) || itemsToKeep.isEmpty() || item.isEmpty()) {
            return false;
        }
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asBukkitMirror(item);
        Iterator<org.bukkit.inventory.ItemStack> iterator = itemsToKeep.iterator();
        while (iterator.hasNext()) {
            if (bukkitStack.equals(iterator.next())) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /** The combat kill packet that opens the death screen, with vanilla's fallback for long messages. */
    @Unique
    private void silkard$sendCombatKillPacket(boolean displayMessage, Component deathMessage) {
        if (displayMessage && deathMessage != CommonComponents.EMPTY) {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), deathMessage), PacketSendListener.exceptionallySend(() -> {
                String truncated = deathMessage.getString(256);
                Component explanation = Component.translatable("death.attack.message_too_long", Component.literal(truncated).withStyle(ChatFormatting.YELLOW));
                Component fakeDeathMessage = Component.translatable("death.attack.even_more_magic", this.getDisplayName())
                    .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(explanation)));
                return new ClientboundPlayerCombatKillPacket(this.getId(), fakeDeathMessage);
            }));
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        }
    }
}
