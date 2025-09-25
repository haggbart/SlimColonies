package no.monopixel.slimcolonies.api.compatibility;

import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for the new furnace recipes.
 */
public interface IFurnaceRecipes
{
    /**
     * Get the smelting result for a certain itemStack.
     *
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    ItemStack getSmeltingResult(final ItemStack itemStack);

    /**
     * Get the first smelting recipe by result for a certain itemStorage.
     *
     * @param storage the itemStorage to test.
     * @return the result or null if not existent.
     */
    IRecipeStorage getFirstSmeltingRecipeByResult(final ItemStorage storage);
}
