package com.mohistmc.silkard.mixin.paper.core;

import com.mohistmc.silkard.injected.paper.core.PaperServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper login time and the Purpur AFK flag on {@link ServerPlayer}. A ServerPlayer is created for
 * every login, so the login time is the creation time of the object that joined.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinPaperServerPlayer implements PaperServerPlayer {

    @Unique private long paper$loginTime = System.currentTimeMillis();
    @Unique private boolean paper$afk;

    @Override
    public long paper$loginTime() {
        return this.paper$loginTime;
    }

    @Override
    public boolean paper$afk() {
        return this.paper$afk;
    }

    /**
     * Purpur - AFK API, with Leaf's AFK title. Fires PlayerAFKEvent, broadcasts the away or back
     * message, tags the tab list name when configured, and updates who counts as sleeping.
     */
    @Override
    public void paper$setAfk(boolean afk) {
        if (this.paper$afk == afk) {
            return;
        }
        ServerPlayer self = (ServerPlayer) (Object) this;

        String msg = afk ? org.purpurmc.purpur.PurpurConfig.afkBroadcastAway : org.purpurmc.purpur.PurpurConfig.afkBroadcastBack;
        org.purpurmc.purpur.event.PlayerAFKEvent event = new org.purpurmc.purpur.event.PlayerAFKEvent(self.getBukkitEntity(), afk, self.level().purpurConfig().idleTimeoutKick, msg, !org.bukkit.Bukkit.isPrimaryThread());
        if (!event.callEvent() || event.shouldKick()) {
            return;
        }

        this.paper$afk = afk;
        if (!afk) {
            self.resetLastActionTime();
        }

        msg = event.getBroadcastMsg();
        if (msg != null && !msg.isEmpty()) {
            String playerName = self.getGameProfile().name();
            if (org.purpurmc.purpur.PurpurConfig.afkBroadcastUseDisplayName) {
                net.kyori.adventure.text.Component displayName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(self.getBukkitEntity().getDisplayName());
                playerName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName);
            }
            self.level().getServer().getPlayerList().broadcastSystemMessage(
                io.papermc.paper.adventure.PaperAdventure.asVanilla(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(String.format(msg, playerName))), false);
        }

        if (self.level().purpurConfig().idleTimeoutUpdateTabList) {
            String scoreboardName = self.getScoreboardName();
            String playerListName = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(self.getBukkitEntity().playerListName());
            String[] split = playerListName.split(java.util.regex.Pattern.quote(scoreboardName)); // Leaf - Sanitize name input
            String prefix = (split.length > 0 ? split[0] : "").replace(org.purpurmc.purpur.PurpurConfig.afkTabListPrefix, "");
            String suffix = (split.length > 1 ? split[1] : "").replace(org.purpurmc.purpur.PurpurConfig.afkTabListSuffix, "");
            if (afk) {
                paper$showAfkTitle(self);
                self.getBukkitEntity().setPlayerListName(org.purpurmc.purpur.PurpurConfig.afkTabListPrefix + prefix + scoreboardName + suffix + org.purpurmc.purpur.PurpurConfig.afkTabListSuffix, true);
            } else {
                self.getBukkitEntity().setPlayerListName(prefix + scoreboardName + suffix, true);
            }
        }

        self.level().updateSleepingPlayerList();
    }

    /** Leaf - Improve Purpur AFK system: title, subtitle and fade-in, stay, fade-out ticks, split by ':'. */
    @Unique
    private static void paper$showAfkTitle(ServerPlayer player) {
        String[] raw = org.purpurmc.purpur.PurpurConfig.afkTitleAway.split(":");
        if (raw.length < 5) return;
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        player.getBukkitEntity().showTitle(net.kyori.adventure.title.Title.title(mm.deserialize(raw[0]), mm.deserialize(raw[1]),
            net.kyori.adventure.title.Title.Times.times(net.kyori.adventure.util.Ticks.duration(Long.parseLong(raw[2])),
                net.kyori.adventure.util.Ticks.duration(Long.parseLong(raw[3])), net.kyori.adventure.util.Ticks.duration(Long.parseLong(raw[4])))));
    }
}
