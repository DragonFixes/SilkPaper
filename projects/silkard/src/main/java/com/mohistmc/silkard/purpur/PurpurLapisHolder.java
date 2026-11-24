package com.mohistmc.silkard.purpur;

/**
 * Purpur - Enchantment Table Persists Lapis: the lapis an enchanting table keeps between uses.
 * Implemented on {@code EnchantingTableBlockEntity} by a mixin.
 */
public interface PurpurLapisHolder {

    int purpur$getLapis();

    void purpur$setLapis(int lapis);
}
