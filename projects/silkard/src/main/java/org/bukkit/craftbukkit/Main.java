package org.bukkit.craftbukkit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;

import static java.util.Arrays.asList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
    public static final java.time.Instant BOOT_TIME = java.time.Instant.now(); // Paper - track initial start time
    public static boolean useJline = true;
    public static boolean useConsole = true;

    // Paper start - Reset loggers after shutdown
    static {
        System.setProperty("java.util.logging.manager", "io.papermc.paper.log.CustomLogManager");
    }
    // Paper end - Reset loggers after shutdown

    public static void main(String[] args) {
        if (System.getProperty("jdk.nio.maxCachedBufferSize") == null) System.setProperty("jdk.nio.maxCachedBufferSize", "262144"); // Paper - cap per-thread NIO cache size; https://www.evanjones.ca/java-bytebuffer-leak.html

        if (System.getProperty("joml.nounsafe") == null) {
            System.setProperty("joml.nounsafe", "true");
        }

        // Todo: Installation script
        OptionParser parser = bukkitOptionParser();

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        }

        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (options.has("v")) {
            System.out.println("Silkard " + org.bukkit.craftbukkit.util.Versioning.getGitHash());
        } else {
            // Do you love Java using + and ! as string based identifiers? I sure do!
            String path = new File(".").getAbsolutePath();
            if (path.contains("!") || path.contains("+")) {
                System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
                return;
            }

            float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));
            if (javaVersion > 70.0) {
                System.err.println("Unsupported Java detected (" + javaVersion + "). Only up to Java 26 is supported.");
                return;
            }

            try {
                useJline = !Terminal.TYPE_DUMB.equals(System.getProperty(TerminalBuilder.PROP_PROVIDER));

                if (options.has("nojline")) {
                    useJline = false;
                }

                if (!useJline) {
                    // This ensures the terminal literal will always match the jline implementation
                    System.setProperty(TerminalBuilder.PROP_PROVIDER, Terminal.TYPE_DUMB);
                }

                if (options.has("noconsole")) {
                    useConsole = false;
                }

                if (Main.class.getPackage().getImplementationVendor() != null && System.getProperty("IReallyKnowWhatIAmDoingISwear") == null) {
                    Date buildDate = new Date(Integer.parseInt(Main.class.getPackage().getImplementationVendor()) * 1000L);

                    Calendar deadline = Calendar.getInstance();
                    deadline.add(Calendar.WEEK_OF_YEAR, -4);
                    if (buildDate.before(deadline.getTime())) {
                        System.err.println("*** Error, this build is outdated ***");
                        System.err.println("*** Please download a new build as per instructions from https://www.spigotmc.org/go/outdated-spigot ***");
                        System.err.println("*** Server will start in 20 seconds ***");
                        Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                    }
                }

                System.out.println("Loading libraries, please wait...");
                // CraftBukkit start - silkard: hand OptionSet over to MainHook, then start vanilla Main
                com.mohistmc.silkard.bukkit.MainHook.configure(options);
                net.minecraft.server.Main.main(new String[0]);
                // CraftBukkit end
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    // silkard: 启动链经 fabric-loader 直达 vanilla Main，CB Main 不会执行；
    // MixinMain 在 vanilla Main.main HEAD 调用 bukkitOptions 注入这份选项
    public static OptionParser bukkitOptionParser() {
        return new OptionParser() {
            {
                this.acceptsAll(asList("?", "help"), "Show the help");

                this.acceptsAll(asList("c", "config"), "Properties file to use")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("server.properties"))
                        .describedAs("Properties file");

                this.acceptsAll(asList("P", "plugins"), "Plugin directory to use")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("plugins"))
                        .describedAs("Plugin directory");

                this.acceptsAll(asList("h", "host", "server-ip"), "Host to listen on")
                        .withRequiredArg()
                        .ofType(String.class)
                        .describedAs("Hostname or IP");

                this.acceptsAll(asList("W", "world-dir", "universe", "world-container"), "World container")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("."))
                        .describedAs("Directory containing worlds");

                this.acceptsAll(asList("w", "world", "level-name"), "World name")
                        .withRequiredArg()
                        .ofType(String.class)
                        .describedAs("World name");

                this.acceptsAll(asList("p", "port", "server-port"), "Port to listen on")
                        .withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("Port");

                this.accepts("serverId", "Server ID")
                        .withRequiredArg();

                this.accepts("jfrProfile", "Enable JFR profiling");

                this.accepts("pidFile", "pid File")
                        .withRequiredArg()
                        .withValuesConvertedBy(new PathConverter());

                this.acceptsAll(asList("o", "online-mode"), "Whether to use online authentication")
                        .withRequiredArg()
                        .ofType(Boolean.class)
                        .describedAs("Authentication");

                this.acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players")
                        .withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("Server size");

                this.acceptsAll(asList("b", "bukkit-settings"), "File for bukkit settings")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("bukkit.yml"))
                        .describedAs("Yml file");

                this.acceptsAll(asList("C", "commands-settings"), "File for command settings")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("commands.yml"))
                        .describedAs("Yml file");

                this.accepts("forceUpgrade", "Whether to force a world upgrade");
                this.accepts("eraseCache", "Whether to force cache erase during world upgrade");
                this.accepts("recreateRegionFiles", "Whether to recreate region files during world upgrade");
                this.accepts("safeMode", "Loads level with vanilla datapack only"); // Paper
                this.accepts("nogui", "Disables the graphical console");

                this.accepts("nojline", "Disables jline and emulates the vanilla console");

                this.accepts("noconsole", "Disables the console");

                this.acceptsAll(asList("v", "version"), "Show the CraftBukkit Version");

                this.accepts("demo", "Demo mode");

                this.accepts("bonusChest", "Enable the bonus chest");

                this.accepts("initSettings", "Only create configuration files and then exit"); // SPIGOT-5761: Add initSettings option

                this.acceptsAll(asList("S", "spigot-settings"), "File for spigot settings")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("spigot.yml"))
                        .describedAs("Yml file");

                this.acceptsAll(asList("paper-dir", "paper-settings-directory"), "Directory for Paper settings")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File(io.papermc.paper.configuration.PaperConfigurations.CONFIG_DIR))
                        .describedAs("Config directory");

                this.acceptsAll(asList("paper", "paper-settings"), "File for Paper settings")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File("paper.yml"))
                        .describedAs("Yml file");

                this.acceptsAll(asList("add-plugin", "add-extra-plugin-jar"), "Specify paths to extra plugin jars to be loaded in addition to those in the plugins folder. This argument can be specified multiple times, once for each extra plugin jar path.")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File[] {})
                        .describedAs("Jar file");

                this.acceptsAll(asList("add-plugin-dir", "add-extra-plugin-dir"), "Specify paths to extra plugin directories to be loaded in addition to the plugins folder. This argument can be specified multiple times, once for each extra plugin dir path.")
                        .withRequiredArg()
                        .ofType(File.class)
                        .defaultsTo(new File[] {})
                        .describedAs("Plugin directory");

                // Purpur start - Purpur config files
                this.acceptsAll(asList("purpur", "purpur-settings"), "File for purpur settings")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("purpur.yml"))
                    .describedAs("Yml file");
                // Purpur end - Purpur config files

                this.accepts("server-name", "Name of the server")
                        .withRequiredArg()
                        .ofType(String.class)
                        .defaultsTo("Unknown Server")
                        .describedAs("Name");
            }
        };
    }

    // 未识别参数放行（vanilla 侧选项 bukkit parser 不认识），解析失败退回默认值，保证注入拿到完整选项集
    public static OptionSet bukkitOptions(String[] args) {
        try {
            OptionParser parser = bukkitOptionParser();
            parser.allowsUnrecognizedOptions();
            return parser.parse(args);
        } catch (joptsimple.OptionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            try {
                return bukkitOptionParser().parse(new String[0]);
            } catch (joptsimple.OptionException ex2) {
                return new OptionParser().parse(new String[0]);
            }
        }
    }
}
