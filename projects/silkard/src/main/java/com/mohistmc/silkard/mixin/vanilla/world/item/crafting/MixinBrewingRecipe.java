package com.mohistmc.silkard.mixin.vanilla.world.item.crafting;

import com.mohistmc.silkard.injected.world.item.crafting.ContextRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftBrewingRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mgazul
 * @date 2026/10/24
 */
@Mixin(BrewingRecipe.class)
public abstract class MixinBrewingRecipe implements ContextRecipe {

    @Shadow
    public abstract ItemStackTemplate getOutput();

    // CraftBukkit start
    @Override
    public org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        BrewingRecipe self = (BrewingRecipe) (Object) this;
        return new org.bukkit.inventory.BrewingRecipe(
            id,
            CraftItemStack.asBukkitCopy(this.getOutput()),
            org.bukkit.craftbukkit.inventory.CraftRecipe.toChoice(self.getInput().ingredient()),
            org.bukkit.craftbukkit.inventory.CraftRecipe.toChoice(self.getReagent().ingredient())
        );
    }
    // CraftBukkit end
}
