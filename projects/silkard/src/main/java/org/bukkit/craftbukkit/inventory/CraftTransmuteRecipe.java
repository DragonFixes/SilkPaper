package org.bukkit.craftbukkit.inventory;

<<<<<<< silkard
import com.mohistmc.silkard.bukkit.BukkitUtils;
import net.minecraft.advancements.predicates.MinMaxBounds;
=======
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
>>>>>>> paper
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.TransmuteRecipe;

public class CraftTransmuteRecipe extends TransmuteRecipe implements CraftRecipe {

    public CraftTransmuteRecipe(NamespacedKey key, Material result, RecipeChoice input, RecipeChoice material) {
        super(key, result, input, material);
    }

    public static CraftTransmuteRecipe fromBukkitRecipe(TransmuteRecipe recipe) {
        if (recipe instanceof CraftTransmuteRecipe) {
            return (CraftTransmuteRecipe) recipe;
        }
        CraftTransmuteRecipe ret = new CraftTransmuteRecipe(recipe.getKey(), recipe.getResult().getType(), recipe.getInput(), recipe.getMaterial());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
<<<<<<< silkard
    public void addToCraftingManager() {
        BukkitUtils.getServer().getRecipeManager().addRecipe(
                new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()),
                        new net.minecraft.world.item.crafting.TransmuteRecipe(getCommon(),
                                CraftRecipe.getBook(this),
                                toNMS(this.getInput(), true),
                                toNMS(this.getMaterial(), true),
                                MinMaxBounds.Ints.between(this.getMinimumMaterialCount(), this.getMaximumMaterialCount()),
                                TransmuteResult.fromTemplate(CraftItemStack.asNMSTemplate(this.getResult())),
                                this.isAddMaterialCountToResult()
                        )
                )
        );
=======
    public RecipeHolder<?> toMinecraftRecipe() {
        return new RecipeHolder<>(CraftNamespacedKey.toResourceKey(Registries.RECIPE, this.getKey()), new net.minecraft.world.item.crafting.TransmuteRecipe(
            new net.minecraft.world.item.crafting.Recipe.CommonInfo(true),
            new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(CraftRecipe.getCategory(this.getCategory()), this.getGroup()),
            CraftRecipe.toIngredient(this.getInput(), true),
            CraftRecipe.toIngredient(this.getMaterial(), true),
            net.minecraft.world.item.crafting.TransmuteRecipe.DEFAULT_MATERIAL_COUNT,
            TransmuteResult.fromTemplate(CraftItemStack.asTemplate(this.getResult())),
            false
        ));
>>>>>>> paper
    }
}
