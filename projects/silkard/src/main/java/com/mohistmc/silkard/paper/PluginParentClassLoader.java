package com.mohistmc.silkard.paper;

/**
 * Parent class loader for every Bukkit/Paper plugin class loader.
 *
 * <p>Delegates everything to the server's class loader except Fabric Loader's own classes.
 * Plugins detect their platform by probing for classes such as
 * {@code net.fabricmc.loader.api.FabricLoader} or {@code net.fabricmc.loader.launch.knot.KnotServer};
 * seeing them, a plugin's Bukkit build takes its Fabric code path (KnockbackSync never creates its
 * scheduler) or refuses to support the server (FastAsyncWorldEdit's "unsafe software" warning).
 * Hiding the loader makes the server look like Paper to plugins, which is what they are running on.
 * Server code loaded by the server's class loader still sees Fabric Loader normally.</p>
 */
public final class PluginParentClassLoader extends ClassLoader {

    private static final String[] HIDDEN_PREFIXES = {
        "net.fabricmc.loader."
    };

    private static volatile PluginParentClassLoader instance;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private PluginParentClassLoader(ClassLoader serverLoader) {
        super("SilkPaper-plugin-parent", serverLoader);
    }

    public static PluginParentClassLoader get() {
        PluginParentClassLoader loader = instance;
        if (loader == null) {
            synchronized (PluginParentClassLoader.class) {
                loader = instance;
                if (loader == null) {
                    loader = new PluginParentClassLoader(PluginParentClassLoader.class.getClassLoader());
                    instance = loader;
                }
            }
        }
        return loader;
    }

    public static boolean isHidden(String className) {
        for (String prefix : HIDDEN_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isHidden(name)) {
            throw new ClassNotFoundException(name);
        }
        return super.loadClass(name, resolve);
    }
}
