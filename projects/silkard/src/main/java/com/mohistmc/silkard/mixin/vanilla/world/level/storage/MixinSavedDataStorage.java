package com.mohistmc.silkard.mixin.vanilla.world.level.storage;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SavedDataStorage.class)
public class MixinSavedDataStorage {

    // Paper - add no-op fix for custom types: Paper's own saved data (paper:level_overrides,
    // paper:metadata, paper:persistent_data_container) has no data fixer, so its SavedDataType
    // carries no DataFixTypes and is read as stored.
    @Redirect(method = "readTagFromDisk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag silkard$skipMissingFixer(DataFixTypes type, DataFixer fixerUpper, CompoundTag tag, int fromVersion, int toVersion) {
        return type == null ? tag : type.update(fixerUpper, tag, fromVersion, toVersion);
    }
}
