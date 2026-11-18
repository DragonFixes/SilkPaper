package org.bukkit.block.data.type;

import org.jetbrains.annotations.NotNull; // Silkard - Spigot API compat
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.jspecify.annotations.NullMarked;

/**
 * 'copper_golem_pose' indicates the pose the statue stands.
 */
@NullMarked
public interface CopperGolemStatue extends BlockData, Directional, Waterlogged {

    /**
     * Gets the value of the 'copper_golem_pose' property.
     *
     * @return the 'copper_golem_pose' value
     */
    Pose getCopperGolemPose();

    /**
     * Sets the value of the 'copper_golem_pose' property.
     *
     * @param pose the new 'copper_golem_pose' value
     */
    void setCopperGolemPose(Pose pose);

    enum Pose {
        STANDING,
        SITTING,
        RUNNING,
        STAR
    }
    // Silkard start - Spigot API compat
    /**
     * Represents the pose of the statute.
     */
    public enum CopperGolemPose {
        /**
         * Standing pose.
         */
        STANDING,
        /**
         * Sitting pose.
         */
        SITTING,
        /**
         * Running pose.
         */
        RUNNING,
        /**
         * Star pose.
         */
        STAR;
    }
    // Silkard end

    // Silkard start - Spigot API compat
    /**
     * Sets the value of the 'copper_golem_pose' property.
     *
     * @param copperGolemPose the new 'copper_golem_pose' value
     */
    void setCopperGolemPose(@NotNull CopperGolemPose copperGolemPose);
    // Silkard end

}
