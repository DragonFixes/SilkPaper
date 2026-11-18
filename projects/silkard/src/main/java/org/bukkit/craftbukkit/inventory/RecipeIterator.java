package org.bukkit.craftbukkit.inventory;

import com.mohistmc.silkard.bukkit.BukkitUtils;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.inventory.Recipe;

public class RecipeIterator implements Iterator<Recipe> {
    private final Iterator<Map.Entry<RecipeType<?>, RecipeHolder<?>>> recipes;
    private RecipeHolder<?> currentRecipe;

    public RecipeIterator() {
        this.recipes = BukkitUtils.getServer().getRecipeManager().recipes.byType.entries().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.recipes.hasNext();
    }

    @Override
    public Recipe next() {
        this.currentRecipe = this.recipes.next().getValue();
        return this.currentRecipe.toBukkitRecipe();
    }

    @Override
    public void remove() {
        com.mohistmc.silkard.bukkit.BukkitUtils.getServer().getRecipeManager().recipes.byKey.remove(this.currentRecipe.id());
        this.recipes.remove();
        com.mohistmc.silkard.bukkit.BukkitUtils.getServer().getRecipeManager().finalizeRecipeLoading();
        com.mohistmc.silkard.bukkit.BukkitUtils.getServer().getPlayerList().reloadRecipes();
    }
}
