package com.mohistmc.silkard.injected.paper.core;

import java.util.function.Predicate;
import net.kyori.adventure.pointer.Pointers;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * State that Paper adds to vanilla item, menu and damage classes. Each nested interface is injected
 * into the class named in its documentation and implemented by the matching
 * {@code MixinPaper*} mixin in {@code com.mohistmc.silkard.mixin.paper.core}.
 */
public final class PaperItemExtras {

    private PaperItemExtras() {
    }

    /** {@code Ingredient}: a predicate recipe choice, with the stack shown for it in recipe books. */
    public interface Ingredient {
        default @Nullable Predicate<ItemStack> paper$stackPredicate() {
            throw new IllegalStateException("Not implemented");
        }

        default @Nullable ItemStack paper$predicateExample() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setStackPredicate(@Nullable Predicate<ItemStack> predicate, @Nullable ItemStack example) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code MerchantOffer}: Paper's switch that keeps villager discounts off this offer. */
    public interface MerchantOffer {
        default boolean paper$ignoreDiscounts() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setIgnoreDiscounts(boolean ignoreDiscounts) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code AnvilMenu}: Paper's anvil overrides. */
    public interface AnvilMenu {
        default boolean paper$bypassEnchantmentLevelRestriction() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setBypassEnchantmentLevelRestriction(boolean bypass) {
            throw new IllegalStateException("Not implemented");
        }

        default boolean paper$bypassCost() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setBypassCost(boolean bypass) {
            throw new IllegalStateException("Not implemented");
        }

        default boolean paper$canDoUnsafeEnchants() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setCanDoUnsafeEnchants(boolean unsafe) {
            throw new IllegalStateException("Not implemented");
        }
    }

    /** {@code DamageSource}: Paper's Adventure damage context. */
    public interface DamageSource {
        default Pointers paper$damageContext() {
            throw new IllegalStateException("Not implemented");
        }

        default void paper$setDamageContext(Pointers context) {
            throw new IllegalStateException("Not implemented");
        }
    }
}
