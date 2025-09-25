package no.monopixel.slimcolonies.core.recipes;

import no.monopixel.slimcolonies.api.MinecoloniesAPIProxy;
import no.monopixel.slimcolonies.api.compatibility.ICompatibilityManager;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.crafting.ModCraftingTypes;
import no.monopixel.slimcolonies.api.crafting.registry.CraftingType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A crafting type for brewing recipes
 */
public class BrewingCraftingType extends CraftingType
{
    public BrewingCraftingType()
    {
        super(ModCraftingTypes.BREWING_ID);
    }

    @Override
    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager, @Nullable Level world)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();
        final ICompatibilityManager compatibilityManager = MinecoloniesAPIProxy.getInstance().getColonyManager().getCompatibilityManager();

        for (final IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
        {
            final List<ItemStack> inputs = compatibilityManager.getListOfAllItems().stream()
                    .filter(recipe::isInput)
                    .collect(Collectors.toList());
            final List<ItemStack> ingredients = compatibilityManager.getListOfAllItems().stream()
                    .filter(recipe::isIngredient)
                    .collect(Collectors.toList());

            for (final ItemStack input : inputs)
            {
                for (final ItemStack ingredient : ingredients)
                {
                    final ItemStack output = recipe.getOutput(input, ingredient);
                    if (!output.isEmpty())
                    {
                        final ItemStack actualInput = input.copy();
                        actualInput.setCount(3);
                        final ItemStack actualOutput = output.copy();
                        actualOutput.setCount(3);

                        recipes.add(GenericRecipe.builder()
                                .withOutput(actualOutput)
                                .withInputs(List.of(List.of(ingredient), List.of(actualInput)))
                                .withIntermediate(Blocks.BREWING_STAND)
                                .build());
                    }
                }
            }
        }

        return recipes;
    }
}
