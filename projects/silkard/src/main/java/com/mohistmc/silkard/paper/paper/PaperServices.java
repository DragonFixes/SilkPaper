package com.mohistmc.silkard.paper.paper;

import io.papermc.paper.profile.PaperFilledProfileCache;

/**
 * Server services Paper adds next to vanilla's {@code Services} record, reached there as
 * {@code services().paper()}.
 */
public final class PaperServices {

    private static final PaperFilledProfileCache FILLED_PROFILE_CACHE = new PaperFilledProfileCache();

    private PaperServices() {
    }

    /** Profiles with textures fetched from the session service, kept to avoid repeated lookups. */
    public static PaperFilledProfileCache filledProfileCache() {
        return FILLED_PROFILE_CACHE;
    }

    /**
     * Leaf's relaxed player name check: 1 to 16 printable ASCII characters other than space, for
     * servers that turn off vanilla's stricter username validation.
     */
    public static boolean isReasonablePlayerName(final String name) {
        if (name.isEmpty() || name.length() > 16) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (c <= ' ' || c >= 0x7F) {
                return false;
            }
        }
        return true;
    }
}
