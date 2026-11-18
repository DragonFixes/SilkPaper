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
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CraftCampfireRecipe extends CampfireRecipe implements CraftRecipe {
    public CraftCampfireRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CraftCampfireRecipe fromBukkitRecipe(CampfireRecipe recipe) {
        if (recipe instanceof CraftCampfireRecipe) {
            return (CraftCampfireRecipe) recipe;
        }
        CraftCampfireRecipe ret = new CraftCampfireRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
<<<<<<< silkard
    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        BukkitUtils.getServer().getRecipeManager().addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.CampfireCookingRecipe(getCommon(), CraftRecipe.getBook(this), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSTemplate(result), getExperience(), getCookingTime())));
=======
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(CraftNamespacedKey.toResourceKey(Registries.RECIPE, this.getKey()), new net.minecraft.world.item.crafting.CampfireCookingRecipe(
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
