package com.mohistmc.silkard.paper;

import com.mohistmc.silkard.bukkit.BukkitUtils;
import com.mohistmc.silkard.injected.server.ContextMinecraftServer;
import io.papermc.paper.configuration.PaperConfigurations;
import io.papermc.paper.configuration.WorldConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import joptsimple.OptionSet;
import net.kyori.adventure.key.Key;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import org.galemc.gale.configuration.GaleConfigurations;
import org.galemc.gale.configuration.GaleWorldConfiguration;
import org.jspecify.annotations.Nullable;
import org.purpurmc.purpur.PurpurConfig;
import org.purpurmc.purpur.PurpurWorldConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spigotmc.SpigotConfig;
import org.spigotmc.SpigotWorldConfig;

/**
 * Starts the Spigot, Paper, Gale, Purpur and Leaf configuration layer.
 *
 * <p>Paper, Gale, Leaf and Purpur keep their files in {@link PaperConfigurations#CONFIG_DIR}
 * ({@code paper-config/}) at the server root, because Fabric owns {@code config/} for mod
 * configs. {@code spigot.yml}, {@code bukkit.yml} and {@code commands.yml} stay at the root,
 * where Bukkit plugins and tooling expect them.
 *
 * <p>Call order matches the upstream servers: {@link #loadEarlyConfigs()} at the start of
 * {@code Main.main} (Leaf loads before vanilla bootstrap), then {@link #initServer} from
 * {@code DedicatedServer.initServer} once the player list exists and before plugins load.
 * Per-world configs are created by {@link #createWorldConfigs} the first time a level asks
 * for one.
 */
public final class PaperBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger("Silkard");

    /** Files that Paper, Gale and Leaf write to their config folder, in their old Fabric-shared location. */
    private static final List<String> LEGACY_CONFIG_FILES = List.of(
        "paper-global.yml",
        "paper-world-defaults.yml",
        "gale-global.yml",
        "gale-world-defaults.yml",
        "leaf-global.yml"
    );
    private static final String LEGACY_CONFIG_DIR = "config";

    private static @Nullable PaperConfigurations paperConfigurations;
    private static @Nullable GaleConfigurations galeConfigurations;
    private static File purpurConfigFile = new File(PaperConfigurations.CONFIG_DIR, "purpur.yml");

    private PaperBootstrap() {
    }

    /** The running server; see {@link BukkitUtils#getServer()}. */
    public static @Nullable MinecraftServer server() {
        return BukkitUtils.getServer();
    }

    public static PaperConfigurations paperConfigurations() {
        return Objects.requireNonNull(paperConfigurations, "Paper configurations are not set up yet");
    }

    public static GaleConfigurations galeConfigurations() {
        return Objects.requireNonNull(galeConfigurations, "Gale configurations are not set up yet");
    }

    /** The purpur.yml in use; {@code /purpur reload} re-reads this file. */
    public static File purpurConfigFile() {
        return purpurConfigFile;
    }

    /**
     * Loads configs that upstream reads before vanilla bootstrap: Leaf's global config, after
     * moving Paper/Gale/Leaf files out of Fabric's {@code config/} folder if they are still there.
     */
    public static void loadEarlyConfigs() {
        // Leaf's config modules read ServerBuildInfo, which needs the game version; this runs at the
        // head of vanilla Main.main, before vanilla detects it. Detection is a no-op once set.
        net.minecraft.SharedConstants.tryDetectVersion();
        migrateLegacyConfigFolder();
        org.dreeam.leaf.config.LeafConfig.loadConfig();
    }

    /**
     * Registers the plugin providers (plugins folder and {@code --add-plugin} jars) with Paper's
     * plugin system, as Paper's {@code Main} does right before vanilla bootstrap. Paper plugin
     * bootstrappers then run from {@code Bootstrap.bootStrap}, and CraftServer loads the
     * providers when it loads plugins.
     */
    public static void discoverPlugins(OptionSet options) {
        // Spigot `libraries:` class loaders sit beside the plugin class loader and are asked
        // for classes it cannot find, so they need the same filtered parent.
        org.bukkit.plugin.java.LibraryLoader.LIBRARY_LOADER_FACTORY =
            (urls, parent) -> new java.net.URLClassLoader(urls, PluginParentClassLoader.get());
        try {
            io.papermc.paper.plugin.PluginInitializerManager.load(options);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize the plugin system", e);
        }
    }

    /**
     * Sets up the server-wide configs and their commands. Runs inside
     * {@code DedicatedServer.initServer}, after {@code setPlayerList} and before plugins load,
     * which is where Spigot, Paper, Gale and Purpur initialize them upstream.
     */
    public static void initServer(DedicatedServer dedicatedServer) throws Exception {
        BukkitUtils.setServer(dedicatedServer);
        final @Nullable OptionSet options = ((ContextMinecraftServer) dedicatedServer).silkard_options();

        // Leaf's post-bootstrap pass (registry-dependent options); vanilla bootstrap has run by now.
        org.dreeam.leaf.config.ConfigModule.loadAfterBootstrap();

        final File spigotFile = optionFile(options, "spigot-settings", new File("spigot.yml"));
        SpigotConfig.init(spigotFile);
        SpigotConfig.registerCommands();

        final Path configDir = optionFile(options, "paper-settings-directory", new File(PaperConfigurations.CONFIG_DIR)).toPath();
        final Path legacyPaperConfig = optionFile(options, "paper-settings", new File("paper.yml")).toPath();
        final Path worldContainer = optionFile(options, "universe", new File(".")).toPath();
        paperConfigurations = PaperConfigurations.setup(legacyPaperConfig, configDir, worldContainer, spigotFile);
        galeConfigurations = GaleConfigurations.setup(configDir);

        paperConfigurations.initializeGlobalConfiguration(dedicatedServer.registryAccess());
        paperConfigurations.initializeWorldDefaultsConfiguration(dedicatedServer.registryAccess());
        galeConfigurations.initializeGlobalConfiguration(dedicatedServer.registryAccess());
        galeConfigurations.initializeWorldDefaultsConfiguration(dedicatedServer.registryAccess());

        purpurConfigFile = optionFile(options, "purpur-settings", new File(PaperConfigurations.CONFIG_DIR, "purpur.yml"));
        migrateRootPurpurConfig(purpurConfigFile);
        PurpurConfig.init(purpurConfigFile);
        PurpurConfig.registerCommands();

        io.papermc.paper.command.PaperCommands.registerCommands(dedicatedServer);
        org.dreeam.leaf.command.LeafCommands.registerCommands(dedicatedServer);
    }

    /** The per-world configs of one level, created together because Paper and Gale read Spigot's. */
    public record WorldConfigs(
        SpigotWorldConfig spigot,
        WorldConfiguration paper,
        PurpurWorldConfig purpur,
        GaleWorldConfiguration gale
    ) {
    }

    /**
     * Creates the Spigot, Paper, Purpur and Gale configs for a level, as the upstream
     * {@code Level} constructor does. Must run after {@link #initServer}.
     */
    public static WorldConfigs createWorldConfigs(ServerLevel level) {
        final String legacyName = legacyWorldName(level);
        final Identifier id = level.dimension().identifier();
        final Key worldKey = Key.key(id.getNamespace(), id.getPath());

        final SpigotWorldConfig spigot = new SpigotWorldConfig(legacyName, worldKey);
        final var context = PaperConfigurations.createWorldContextMap(
            level.silkard_storageSource().getDimensionPath(level.dimension()), id, spigot, level.registryAccess(), level.getGameRules());
        final WorldConfiguration paper = paperConfigurations().createWorldConfig(context);
        final PurpurWorldConfig purpur = new PurpurWorldConfig(legacyName, level.silkard$environment(), worldKey);
        final GaleWorldConfiguration gale = galeConfigurations().createWorldConfig(context);
        return new WorldConfigs(spigot, paper, purpur, gale);
    }

    /**
     * The CraftBukkit-era world name used for per-world sections in spigot.yml and purpur.yml:
     * the level name for the overworld, {@code <name>_nether} and {@code <name>_the_end} for the
     * vanilla dimensions, and the dimension path for any other (plugin or mod) dimension.
     */
    public static String legacyWorldName(Level level) {
        final String levelName = ((ServerLevelData) level.getLevelData()).getLevelName();
        if (level.dimension() == Level.OVERWORLD) {
            return levelName;
        }
        if (level.dimension() == Level.NETHER) {
            return levelName + "_nether";
        }
        if (level.dimension() == Level.END) {
            return levelName + "_the_end";
        }
        return level.dimension().identifier().getPath();
    }

    /**
     * Reads a file option registered by CraftBukkit's Main. Options are only present when the
     * launcher parsed them, so a missing option or value falls back to {@code fallback}.
     */
    private static File optionFile(@Nullable OptionSet options, String name, File fallback) {
        if (options == null) {
            return fallback;
        }
        try {
            final Object value = options.valueOf(name);
            return value instanceof File file ? file : fallback;
        } catch (RuntimeException unknownOption) {
            return fallback;
        }
    }

    /**
     * Moves the files Paper, Gale and Leaf own from {@code config/} to {@code paper-config/}
     * when {@code paper-config/} does not exist yet. Only those named files move; everything
     * else in {@code config/} belongs to Fabric mods and is left alone.
     */
    private static void migrateLegacyConfigFolder() {
        final Path legacyDir = Path.of(LEGACY_CONFIG_DIR);
        final Path targetDir = Path.of(PaperConfigurations.CONFIG_DIR);
        if (Files.exists(targetDir) || !Files.isRegularFile(legacyDir.resolve("paper-global.yml"))) {
            return;
        }
        try {
            Files.createDirectories(targetDir);
            for (String name : LEGACY_CONFIG_FILES) {
                final Path source = legacyDir.resolve(name);
                if (Files.isRegularFile(source)) {
                    Files.move(source, targetDir.resolve(name), StandardCopyOption.ATOMIC_MOVE);
                    LOGGER.info("Moved {} to {}", source, targetDir.resolve(name));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not move Paper/Gale/Leaf configs from {} to {}; they will be regenerated with defaults", legacyDir, targetDir, e);
        }
    }

    /** Moves a root-level purpur.yml (Purpur's own default location) into paper-config/ once. */
    private static void migrateRootPurpurConfig(File target) {
        final Path rootFile = Path.of("purpur.yml");
        final Path targetPath = target.toPath();
        if (Files.exists(targetPath) || !Files.isRegularFile(rootFile) || rootFile.toAbsolutePath().equals(targetPath.toAbsolutePath())) {
            return;
        }
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }
            Files.move(rootFile, targetPath);
            LOGGER.info("Moved {} to {}", rootFile, targetPath);
        } catch (IOException e) {
            LOGGER.error("Could not move {} to {}", rootFile, targetPath, e);
        }
    }
}
