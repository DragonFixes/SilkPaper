package com.mohistmc.silkard.paper.server;

import ca.spottedleaf.common.time.TickData;
import java.util.Locale;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Server-wide state and lookups that Paper, Purpur and Gale patch into {@code MinecraftServer},
 * {@code DedicatedServer} and the profile cache, provided here for Paper's CraftServer.
 */
public final class PaperServerHooks {

    /**
     * Paper's {@code BedSleepingProblem.EXPLOSION}: the bed exploded instead of letting the player
     * sleep. Vanilla reports that case by exploding in {@code BedBlock} without returning a
     * problem, so this value is a sentinel nothing in vanilla produces.
     */
    public static final net.minecraft.world.entity.player.Player.BedSleepingProblem BED_EXPLOSION =
        new net.minecraft.world.entity.player.Player.BedSleepingProblem(net.minecraft.network.chat.Component.translatable("block.minecraft.bed.explode"));

    /** Duration of the last completed server tick in milliseconds (Gale's "proper" tick time). */
    private static volatile long lastTickProperTime;

    private PaperServerHooks() {
    }

    /**
     * Looks a name up in the profile cache only, never asking the session server.
     */
    public static @Nullable NameAndId nameToIdIfCached(final UserNameToIdResolver resolver, final String name) {
        if (resolver instanceof CachedUserNameToIdResolver cached) {
            final Map<String, CachedUserNameToIdResolver.GameProfileInfo> byName = cached.profilesByName;
            final CachedUserNameToIdResolver.GameProfileInfo info = byName.get(name.toLowerCase(Locale.ROOT));
            return info == null ? null : info.nameAndId();
        }
        return null;
    }

    public static TickData.@Nullable MSPTData msptData5s(final MinecraftServer server) {
        return server.paper$msptData5s();
    }

    public static void allowPausing(final Plugin plugin, final boolean value) {
        // TODO Silkard - vanilla's pause-when-empty has no per-plugin override; the request is ignored
    }

    /** Purpur's {@code server-name} server.properties entry. */
    public static String serverName() {
        final DedicatedServer server = (DedicatedServer) com.mohistmc.silkard.bukkit.BukkitUtils.getServer();
        return server.getProperties().properties.getProperty("server-name", "Unknown Server");
    }

    /** Purpur's lagging flag: the last one-minute TPS average is under the configured threshold. */
    public static boolean isLagging(final MinecraftServer server) {
        return server.getTPS()[0] < org.purpurmc.purpur.PurpurConfig.laggingThreshold;
    }

    public static long lastTickProperTime(final MinecraftServer server) {
        return lastTickProperTime;
    }

    public static long lastTickOversleepTime(final MinecraftServer server) {
        // TODO Silkard - the tick wait loop is not instrumented, so oversleep time is not measured
        return 0L;
    }

    public static void recordTickTime(final long nanos) {
        lastTickProperTime = nanos / 1_000_000L;
    }
}
