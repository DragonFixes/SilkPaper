package com.mohistmc.silkard.paper.server;

import com.mohistmc.silkard.bukkit.BukkitUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Paper's static {@link TagValueOutput}/{@link TagValueInput} factories. Mixins cannot add
 * static methods to vanilla classes, so Paper-derived code calls these instead.
 */
public final class PaperValueIO {

    private PaperValueIO() {
    }

    /**
     * Creates an output that writes into {@code output} without registry context.
     */
    public static TagValueOutput createWrappingGlobal(final ProblemReporter problemReporter, final CompoundTag output) {
        return new TagValueOutput(problemReporter, NbtOps.INSTANCE, output);
    }

    /**
     * Creates an output that writes into {@code output} using {@code lookup} for registry-aware codecs.
     */
    public static TagValueOutput createWrappingWithContext(final ProblemReporter problemReporter, final HolderLookup.Provider lookup, final CompoundTag output) {
        return new TagValueOutput(problemReporter, lookup.createSerializationContext(NbtOps.INSTANCE), output);
    }

    /**
     * Creates an input over {@code compoundTag} using the running server's registries.
     */
    public static ValueInput createGlobal(final ProblemReporter problemReporter, final CompoundTag compoundTag) {
        return TagValueInput.create(problemReporter, BukkitUtils.getServer().registryAccess(), compoundTag);
    }
}
