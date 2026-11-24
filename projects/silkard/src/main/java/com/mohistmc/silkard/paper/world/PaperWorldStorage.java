package com.mohistmc.silkard.paper.world;

import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

/**
 * Paper's dimension-scoped saved data reads. Paper keeps each dimension's own saved data
 * (world gen settings, {@code paper:level_overrides}, {@code paper:metadata}, the world PDC) in
 * {@code <world>/dimensions/<namespace>/<path>/data/} instead of the save's root {@code data/}.
 *
 * <p>{@code LevelStorageSource.readExistingSavedData(access, dimension, registries, type)}, the
 * overload Paper adds and plugins call, delegates here (see {@code MixinLevelStorageSource}).</p>
 */
public final class PaperWorldStorage {

    private PaperWorldStorage() {
    }

    public static Path dimensionDataPath(LevelStorageSource.LevelStorageAccess access, ResourceKey<Level> dimension) {
        return access.getDimensionPath(dimension).resolve(LevelResource.DATA.id());
    }

    public static <T extends SavedData> DataResult<T> readExistingSavedData(
        LevelStorageSource.LevelStorageAccess access,
        ResourceKey<Level> dimension,
        HolderLookup.Provider registryAccess,
        SavedDataType<T> savedDataType
    ) {
        Path dataLocation = savedDataType.id().withSuffix(".dat").resolveAgainst(dimensionDataPath(access, dimension));

        CompoundTag fileContents;
        try {
            fileContents = NbtIo.readCompressed(dataLocation, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            return DataResult.error(e::getMessage);
        }

        return savedDataType.codec().parse(RegistryOps.create(NbtOps.INSTANCE, registryAccess), fileContents.getCompoundOrEmpty("data"));
    }
}
