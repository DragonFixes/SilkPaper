package org.bukkit.entity;

import org.bukkit.entity.model.PlayerModelPart; // Silkard - Spigot API compat
import org.bukkit.profile.PlayerProfile; // Silkard - Spigot API compat
import org.jetbrains.annotations.NotNull; // Silkard - Spigot API compat
import com.destroystokyo.paper.SkinParts;
import io.papermc.paper.InternalAPIBridge;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.MainHand;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Mannequin extends LivingEntity {

    /**
     * Returns the valid poses for a mannequin.
     *
     * @return the valid poses
     */
    static Set<Pose> validPoses() {
        return InternalAPIBridge.get().validMannequinPoses();
    }

    /**
     * Returns the default mannequin profile.
     *
     * @return the default mannequin profile
     */
    static ResolvableProfile defaultProfile() {
        return InternalAPIBridge.get().defaultMannequinProfile();
    }

    /**
     * Returns the default mannequin description.
     *
     * @return the default mannequin description
     */
    static Component defaultDescription() {
        return InternalAPIBridge.get().defaultMannequinDescription();
    }

    /**
     * Gets the resolvable profile for this mannequin.
     *
     * @return the resolvable profile
     */
    ResolvableProfile getProfile();

    /**
     * Sets the resolvable profile for this mannequin.
     *
     * @param profile the new resolvable profile
     */
    void setProfile(ResolvableProfile profile);

    /**
     * Gets a copy of the current skin part options for this mannequin.
     *
     * @return a copy of the current skin part options
     */
    SkinParts.Mutable getSkinParts();

    /**
     * Sets the skin part options for this mannequin.
     *
     * @param parts the new skin part options
     */
    void setSkinParts(SkinParts parts);

    /**
     * Checks if this mannequin is immovable.
     *
     * @return whether this mannequin is immovable
     */
    boolean isImmovable();

    /**
     * Sets whether this mannequin is immovable.
     *
     * @param immovable new immovable state
     */
    void setImmovable(boolean immovable);

    /**
     * Gets the description text for this mannequin (appears below the name).
     *
     * @return the description, or null if none is set
     */
    @Nullable Component getDescription();

    /**
     * Sets the description text for this mannequin (appears below the name).
     * Setting the description to null will remove it.
     *
     * @param description the new description, or null to remove it
     */
    void setDescription(@Nullable Component description);

    /**
     * Gets the main hand of this mannequin.
     *
     * @return the main hand
     */
    MainHand getMainHand();

    /**
     * Sets the main hand of this mannequin.
     *
     * @param hand the new main hand
     */
    void setMainHand(MainHand hand);

    @Override
    EntityEquipment getEquipment();
    // Silkard start - Spigot API compat
    /**
     * Gets whether a part of the mannequin model is shown.
     *
     * @param part model part
     * @return if it is shown
     */
    boolean isModelPartShown(@NotNull PlayerModelPart part);

    /**
     * Sets the description which shows as part of the mannequin's name.
     *
     * @param description the description to show or null for default
     */
    void setDescription(@Nullable String description);

    /**
     * Gets the profile of the player used to texture the mannequin.
     *
     * @return the profile of the owning player
     */
    @Nullable
    PlayerProfile getPlayerProfile();

    /**
     * Sets the profile of the player used to texture the mannequin.
     * <p>
     * The profile must already contain a skin texture for it to be displayed.
     *
     * @param profile the profile of the player texture.
     * @throws IllegalArgumentException if the profile does not contain the
     * necessary information
     */
    void setPlayerProfile(@Nullable PlayerProfile profile);

    /**
     * Sets whether the mannequin description is hidden.
     *
     * @param hide whether to hide description
     */
    void setHideDescription(boolean hide);

    /**
     * Gets the description which shows as part of the mannequin's name.
     *
     * @return description the description text
     * @deprecated typo, use {@link #getDescription()}
     */
    @Nullable
    @Deprecated(since = "1.21.11")
    String getDescripion();

    /**
     * Sets whether a part of the mannequin model is shown.
     *
     * @param part model part
     * @param shown whether it is shown
     */
    void setModelPartShown(@NotNull PlayerModelPart part, boolean shown);

    /**
     * Gets whether the mannequin description is hidden.
     *
     * @return hide description status
     */
    boolean isHideDescription();
    // Silkard end

}
