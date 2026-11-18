package org.bukkit.inventory;

import io.papermc.paper.datacomponent.DataComponentType;

/**
 * A ItemFlag can hide some Attributes from ItemStacks
 * @apiNote Setting these without also setting the data they are hiding
 * may not result in the item flag being persisted in the ItemMeta/ItemStack.
 */
public enum ItemFlag {

    /**
     * Setting to show/hide enchants
     */
    HIDE_ENCHANTS,
    /**
     * Setting to show/hide Attributes like Damage
     */
    HIDE_ATTRIBUTES,
    /**
     * Setting to show/hide the unbreakable State
     */
    HIDE_UNBREAKABLE,
    /**
     * Setting to show/hide what the ItemStack can break/destroy
     */
    HIDE_DESTROYS,
    /**
     * Setting to show/hide where this ItemStack can be build/placed on
     */
    HIDE_PLACED_ON,
    /**
     * Setting to show/hide item-specific information, including, but not limited to:
     * <ul>
     *     <li>Potion effects on potions, tipped arrows, and suspicious stew</li>
     *     <li>Enchanted book enchantments</li>
     *     <li>Book author and generation</li>
     *     <li>Record names</li>
     *     <li>Patterns of banners and shields</li>
     *     <li>Fish bucket variants</li>
     *     <li>Instrument item descriptions (i.e. goat horn sounds)</li>
     *     <li>Map data</li>
     *     <li>Firework data</li>
     *     <li>Crossbow projectile info</li>
     *     <li>Bundle fullness</li>
     *     <li>Shulker box contents</li>
     *     <li>Spawner descriptions</li>
     * </ul>
     *
     * @see #HIDE_STORED_ENCHANTS HIDE_STORED_ENCHANTS for hiding stored enchants (like on enchanted books)
     * @deprecated does not exist anymore and will not properly work with individually hidden data components; see {@link io.papermc.paper.datacomponent.item.TooltipDisplay} and {@link ItemStack#setData(DataComponentType.Valued, Object)}
     */
    @Deprecated
    HIDE_ADDITIONAL_TOOLTIP,
    /**
     * Setting to show/hide dyes from colored leather armor.
     */
    HIDE_DYE,
    /**
     * Setting to show/hide armor trim from armor.
     */
    HIDE_ARMOR_TRIM,
    /**
     * Setting to show/hide stored enchants on an item, such as enchantments
     * on an enchanted book.
     */
    HIDE_STORED_ENCHANTS,
    // Silkard start - Spigot API compat
    // Component flags
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CUSTOM_DATA("custom_data"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MAX_STACK_SIZE("max_stack_size"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MAX_DAMAGE("max_damage"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_DAMAGE("damage"),
    // @org.jetbrains.annotations.ApiStatus.Experimental
    // HIDE_UNBREAKABLE("unbreakable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CUSTOM_NAME("custom_name"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ITEM_NAME("item_name"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ITEM_MODEL("item_model"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_LORE("lore"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_RARITY("rarity"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ENCHANTMENTS("enchantments"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CAN_PLACE_ON("can_place_on"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CAN_BREAK("can_break"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ATTRIBUTE_MODIFIERS("attribute_modifiers"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CUSTOM_MODEL_DATA("custom_model_data"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TOOLTIP_DISPLAY("tooltip_display"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_REPAIR_COST("repair_cost"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CREATIVE_SLOT_LOCK("creative_slot_lock"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ENCHANTMENT_GLINT_OVERRIDE("enchantment_glint_override"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_INTANGIBLE_PROJECTILE("intangible_projectile"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_FOOD("food"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CONSUMABLE("consumable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_USE_REMAINDER("use_remainder"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_USE_COOLDOWN("use_cooldown"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_DAMAGE_RESISTANT("damage_resistant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TOOL("tool"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WEAPON("weapon"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ENCHANTABLE("enchantable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_EQUIPPABLE("equippable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_REPAIRABLE("repairable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_GLIDER("glider"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TOOLTIP_STYLE("tooltip_style"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_DEATH_PROTECTION("death_protection"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BLOCKS_ATTACKS("blocks_attacks"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_STORED_ENCHANTMENTS("stored_enchantments"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_DYED_COLOR("dyed_color"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MAP_ID("map_id"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MAP_DECORATIONS("map_decorations"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MAP_POST_PROCESSING("map_post_processing"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CHARGED_PROJECTILES("charged_projectiles"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BUNDLE_CONTENTS("bundle_contents"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_POTION_CONTENTS("potion_contents"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_POTION_DURATION_SCALE("potion_duration_scale"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_SUSPICIOUS_STEW_EFFECTS("suspicious_stew_effects"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WRITABLE_BOOK_CONTENT("writable_book_content"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WRITTEN_BOOK_CONTENT("written_book_content"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TRIM("trim"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_DEBUG_STICK_STATE("debug_stick_state"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_ENTITY_DATA("entity_data"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BUCKET_ENTITY_DATA("bucket_entity_data"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BLOCK_ENTITY_DATA("block_entity_data"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_INSTRUMENT("instrument"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PROVIDES_TRIM_MATERIAL("provides_trim_material"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_OMINOUS_BOTTLE_AMPLIFIER("ominous_bottle_amplifier"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_JUKEBOX_PLAYABLE("jukebox_playable"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PROVIDES_BANNER_PATTERNS("provides_banner_patterns"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_RECIPES("recipes"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_LODESTONE_TRACKER("lodestone_tracker"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_FIREWORK_EXPLOSION("firework_explosion"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_FIREWORKS("fireworks"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PROFILE("profile"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_NOTE_BLOCK_SOUND("note_block_sound"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BANNER_PATTERNS("banner_patterns"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BASE_COLOR("base_color"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_POT_DECORATIONS("pot_decorations"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CONTAINER("container"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BLOCK_STATE("block_state"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BEES("bees"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_LOCK("lock"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CONTAINER_LOOT("container_loot"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_BREAK_SOUND("break_sound"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_VILLAGER_VARIANT("villager/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WOLF_VARIANT("wolf/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WOLF_SOUND_VARIANT("wolf/sound_variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_WOLF_COLLAR("wolf/collar"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_FOX_VARIANT("fox/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_SALMON_SIZE("salmon/size"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PARROT_VARIANT("parrot/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TROPICAL_FISH_PATTERN("tropical_fish/pattern"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TROPICAL_FISH_BASE_COLOR("tropical_fish/base_color"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_TROPICAL_FISH_PATTERN_COLOR("tropical_fish/pattern_color"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_MOOSHROOM_VARIANT("mooshroom/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_RABBIT_VARIANT("rabbit/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PIG_VARIANT("pig/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_COW_VARIANT("cow/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CHICKEN_VARIANT("chicken/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_FROG_VARIANT("frog/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_HORSE_VARIANT("horse/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_PAINTING_VARIANT("painting/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_LLAMA_VARIANT("llama/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_AXOLOTL_VARIANT("axolotl/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CAT_VARIANT("cat/variant"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_CAT_COLLAR("cat/collar"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_SHEEP_COLOR("sheep/color"),
    @org.jetbrains.annotations.ApiStatus.Experimental
    HIDE_SHULKER_COLOR("shulker/color");

    private final org.bukkit.NamespacedKey component;

    private ItemFlag() {
        this.component = null;
    }

    private ItemFlag(String component) {
        this.component = org.bukkit.NamespacedKey.minecraft(component);
    }

    @org.jetbrains.annotations.ApiStatus.Internal
    public org.bukkit.NamespacedKey getComponent() {
        return this.component;
    }
    // Silkard end
}
