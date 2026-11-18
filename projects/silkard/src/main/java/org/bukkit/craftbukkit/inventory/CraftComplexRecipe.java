package org.bukkit.craftbukkit.inventory;

<<<<<<< silkard
import com.mohistmc.silkard.bukkit.BukkitUtils;
=======
import net.minecraft.core.registries.Registries;
>>>>>>> paper
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;

public class CraftComplexRecipe extends CraftingRecipe implements CraftRecipe, ComplexRecipe {

    private final net.minecraft.world.item.crafting.CraftingRecipe recipe;

    public CraftComplexRecipe(NamespacedKey key, ItemStack result, net.minecraft.world.item.crafting.CraftingRecipe recipe) {
        super(key, result);
        this.recipe = recipe;
    }

    @Override
<<<<<<< silkard
    public void addToCraftingManager() {
        BukkitUtils.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), recipe));
=======
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(CraftNamespacedKey.toResourceKey(Registries.RECIPE, this.getKey()), recipe);
>>>>>>> paper
    }
}
