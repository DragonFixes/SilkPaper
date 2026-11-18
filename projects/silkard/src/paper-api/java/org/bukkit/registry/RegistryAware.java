package org.bukkit.registry;

import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that instances of a class may be registered to the server and have a key associated with them.
 *
 * @see Registry
 */
public interface RegistryAware {

    /**
     * Gets the key of this object if it is registered, throwing otherwise.
     *
     * @return the key with which this object is registered.
     * @throws IllegalStateException if this object is not registered.
     * @see #isRegistered()
     * @see Registry
     */
    @NotNull
    // Silkard start - Spigot API compat: derive from Keyed so Paper types need no per-type implementation
    default NamespacedKey getKeyOrThrow() {
        NamespacedKey key = this.getKeyOrNull();
        Preconditions.checkState(key != null, "Cannot get key of this registry item, because it is not registered. Use #isRegistered() before calling this method.");
        return key;
    }
    // Silkard end

    /**
     * Gets the key of this object if it is registered, otherwise null.
     *
     * @return the key with which this object is registered or null.
     * @see #isRegistered()
     * @see Registry
     */
    @Nullable
    // Silkard start - Spigot API compat
    default NamespacedKey getKeyOrNull() {
        if (this instanceof Keyed keyed) {
            try {
                return keyed.getKey();
            } catch (RuntimeException unregistered) {
                return null;
            }
        }
        return null;
    }
    // Silkard end

    /**
     * Returns whether this object is registered.
     *
     * @return true if registered.
     * @see Registry
     */
    // Silkard start - Spigot API compat
    default boolean isRegistered() {
        return this.getKeyOrNull() != null;
    }
    // Silkard end
}