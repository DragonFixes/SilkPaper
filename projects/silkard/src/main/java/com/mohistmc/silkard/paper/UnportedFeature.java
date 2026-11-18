package com.mohistmc.silkard.paper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaf/Purpur/Gale config options whose implementation Silkard does not ship yet.
 *
 * <p>The option stays in the config file so existing files keep loading unchanged, but an enabled
 * value is reported once and forced off: these features replace vanilla threading or storage, and
 * half-enabling them (config on, implementation missing) would let other code take the wrong path.
 */
public final class UnportedFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("Silkard");
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private UnportedFeature() {
    }

    /**
     * Returns {@code false}, logging once per option when the configured value was {@code true}.
     *
     * @param option the option's config path, for the log message
     * @param configured the value read from the config
     * @return always {@code false}
     */
    public static boolean disable(String option, boolean configured) {
        if (configured && REPORTED.add(option)) {
            LOGGER.warn("{} is enabled in the config but is not supported by Silkard yet; it stays disabled.", option);
        }
        return false;
    }
}
