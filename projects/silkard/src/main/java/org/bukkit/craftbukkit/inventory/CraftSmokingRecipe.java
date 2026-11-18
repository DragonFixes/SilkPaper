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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

public class CraftSmokingRecipe extends SmokingRecipe implements CraftRecipe {
    public CraftSmokingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CraftSmokingRecipe fromBukkitRecipe(SmokingRecipe recipe) {
        if (recipe instanceof CraftSmokingRecipe) {
            return (CraftSmokingRecipe) recipe;
        }
        CraftSmokingRecipe ret = new CraftSmokingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
<<<<<<< silkard
    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        BukkitUtils.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.SmokingRecipe(getCommon(), CraftRecipe.getBook(this), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSTemplate(result), getExperience(), getCookingTime())));
=======
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(CraftNamespacedKey.toResourceKey(Registries.RECIPE, this.getKey()), new net.minecraft.world.item.crafting.SmokingRecipe(
            new net.minecraft.world.item.crafting.Recipe.CommonInfo(true),
            new net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo(CraftRecipe.getCategory(this.getCategory()), this.getGroup()),
            CraftRecipe.toIngredient(this.getInputChoice(), true),
            CraftItemStack.asTemplate(this.getResult()),
            this.getExperience(),
            this.getCookingTime()
        ));
>>>>>>> paper
    }
}
