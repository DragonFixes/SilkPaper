package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.DyeRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CraftDyeRecipe extends DyeRecipe implements CraftRecipe {

    public CraftDyeRecipe(NamespacedKey key, ItemStack result, RecipeChoice input, RecipeChoice material) {
        super(key, result, input, material);
    }

    public static CraftDyeRecipe fromBukkitRecipe(DyeRecipe recipe) {
        if (recipe instanceof CraftDyeRecipe) {
            return (CraftDyeRecipe) recipe;
        }
        CraftDyeRecipe ret = new CraftDyeRecipe(recipe.getKey(), recipe.getResult(), recipe.getInput(), recipe.getMaterial());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(org.bukkit.craftbukkit.util.CraftNamespacedKey.toResourceKey(net.minecraft.core.registries.Registries.RECIPE, this.getKey()),
            new net.minecraft.world.item.crafting.DyeRecipe(
                new net.minecraft.world.item.crafting.Recipe.CommonInfo(true),
                new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(CraftRecipe.getCategory(this.getCategory()), this.getGroup()),
                CraftRecipe.toIngredient(this.getInput(), true),
                CraftRecipe.toIngredient(this.getMaterial(), true),
                CraftItemStack.asTemplate(this.getResult())
            ));
    }
}
