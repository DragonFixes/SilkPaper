package com.mohistmc.silkard.paper.paper;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * The enchanting table's offer roll, which Paper splits out of {@code EnchantmentMenu.slotsChanged}
 * as {@code fillEnchantmentOffers}. Mirrors vanilla's algorithm for the three offer slots.
 */
public final class Enchanting {

    private Enchanting() {
    }

    public static void fillEnchantmentOffers(final RandomSource random, final int enchantmentSeed, final int bookcases, final ItemStack itemStack,
                                             final RegistryAccess registryAccess, final int[] costs, final int[] enchantClue, final int[] levelClue) {
        final IdMap<Holder<Enchantment>> holders = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
        random.setSeed(enchantmentSeed);

        for (int slot = 0; slot < costs.length; slot++) {
            costs[slot] = EnchantmentHelper.getEnchantmentCost(random, slot, bookcases, itemStack);
            enchantClue[slot] = -1;
            levelClue[slot] = -1;
            if (costs[slot] < slot + 1) {
                costs[slot] = 0;
            }
        }

        for (int slot = 0; slot < costs.length; slot++) {
            if (costs[slot] > 0) {
                final List<EnchantmentInstance> list = getEnchantmentList(random, enchantmentSeed, registryAccess, itemStack, slot, costs[slot]);
                if (!list.isEmpty()) {
                    final EnchantmentInstance instance = list.get(random.nextInt(list.size()));
                    enchantClue[slot] = holders.getId(instance.enchantment());
                    levelClue[slot] = instance.level();
                }
            }
        }
    }

    private static List<EnchantmentInstance> getEnchantmentList(final RandomSource random, final int enchantmentSeed, final RegistryAccess access,
                                                                final ItemStack itemStack, final int slot, final int enchantmentCost) {
        random.setSeed(enchantmentSeed + slot);
        final Optional<HolderSet.Named<Enchantment>> tag = access.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (tag.isEmpty()) {
            return List.of();
        }
        final List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, itemStack, enchantmentCost, tag.get().stream());
        if (itemStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(random.nextInt(list.size()));
        }
        return list;
    }
}
