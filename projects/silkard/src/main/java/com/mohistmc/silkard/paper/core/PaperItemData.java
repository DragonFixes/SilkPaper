package com.mohistmc.silkard.paper.core;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

/**
 * Static counterparts of item data helpers that Paper adds to vanilla component and storage
 * classes.
 */
public final class PaperItemData {

    private static final String ID_TAG = "id";

    private PaperItemData() {
    }

    /** The stored entity data with its {@code id} entry, as it would appear in an entity save. */
    public static CompoundTag copyTagWithEntityId(TypedEntityData<EntityType<?>> data) {
        CompoundTag tag = data.copyTagWithoutId();
        tag.putString(ID_TAG, EntityType.getKey(data.type()).toString());
        return tag;
    }

    /** The stored block entity data with its {@code id} entry, as it would appear in a block entity save. */
    public static CompoundTag copyTagWithBlockEntityId(TypedEntityData<BlockEntityType<?>> data) {
        CompoundTag tag = data.copyTagWithoutId();
        Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(data.type());
        if (id != null) {
            tag.putString(ID_TAG, id.toString());
        }
        return tag;
    }

    /** Typed entity data from a tag carrying an entity {@code id}; an unknown id falls back to the registry default. */
    public static TypedEntityData<EntityType<?>> decodeEntity(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr(ID_TAG, ""));
        return TypedEntityData.of(BuiltInRegistries.ENTITY_TYPE.getValue(id), tag);
    }

    /** Typed block entity data from a tag carrying a block entity {@code id}. */
    public static TypedEntityData<BlockEntityType<?>> decodeBlockEntity(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr(ID_TAG, ""));
        BlockEntityType<?> type = id == null ? null : BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id);
        if (type == null) {
            throw new IllegalArgumentException("Block entity data has no valid id: " + tag);
        }
        return TypedEntityData.of(type, tag);
    }

    /** An output that writes into {@code tag} itself, with registry context. */
    public static TagValueOutput createWrappingWithContext(ProblemReporter reporter, HolderLookup.Provider registries, CompoundTag tag) {
        return new TagValueOutput(reporter, registries.createSerializationContext(NbtOps.INSTANCE), tag);
    }

    /** The custom data tag itself, without a copy. Callers must not modify it. */
    public static CompoundTag unsafeTag(CustomData data) {
        return data.tag;
    }

    /** Adds an attribute instance the map's supplier did not provide; existing instances are kept. */
    public static void registerAttribute(AttributeMap map, Holder<Attribute> attribute) {
        map.attributes.computeIfAbsent(attribute, holder -> new AttributeInstance(holder, map::onAttributeModified));
    }

    /** A loot context with no level, for evaluating level-independent providers. */
    public static LootContext contextWithoutLevel(LootParams params, long seed, HolderLookup.Provider registries) {
        return new LootContext(params, RandomSource.create(seed), registries);
    }
}
