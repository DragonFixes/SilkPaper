package org.bukkit.craftbukkit.inventory;

<<<<<<< silkard
import com.mohistmc.silkard.bukkit.BukkitUtils;
=======
import net.minecraft.core.registries.Registries;
>>>>>>> paper
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

public class CraftStonecuttingRecipe extends StonecuttingRecipe implements CraftRecipe {
    public CraftStonecuttingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source) {
        super(key, result, source);
    }

    public static CraftStonecuttingRecipe fromBukkitRecipe(StonecuttingRecipe recipe) {
        if (recipe instanceof CraftStonecuttingRecipe stonecuttingRecipe) {
            return stonecuttingRecipe;
        }
        return new CraftStonecuttingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice());
    }

    @Override
<<<<<<< silkard
    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        BukkitUtils.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.StonecutterRecipe(getCommon(), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSTemplate(result))));
=======
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(CraftNamespacedKey.toResourceKey(Registries.RECIPE, this.getKey()), new net.minecraft.world.item.crafting.StonecutterRecipe(
            new net.minecraft.world.item.crafting.Recipe.CommonInfo(true),
            CraftRecipe.toIngredient(this.getInputChoice(), true),
            CraftItemStack.asTemplate(this.getResult())
        ));
>>>>>>> paper
    }
}
