package no.monopixel.slimcolonies.core.generation.defaults.workers;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Chef.
 */
public class DefaultChefCraftingProvider extends CustomRecipeProvider
{
    private static final String CHEF = ModJobs.CHEF_ID.getPath();

    public DefaultChefCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultChefCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "mushroom_stew")
            .inputs(List.of(
                new ItemStorage(new ItemStack(Items.RED_MUSHROOM)),
                new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
                new ItemStorage(new ItemStack(Items.BOWL))))
            .result(new ItemStack(Items.MUSHROOM_STEW))
            .minBuildingLevel(1)
            .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "rabbit_stew")
            .inputs(List.of(
                new ItemStorage(new ItemStack(Items.COOKED_RABBIT)),
                new ItemStorage(new ItemStack(Items.CARROT)),
                new ItemStorage(new ItemStack(Items.BAKED_POTATO)),
                new ItemStorage(new ItemStack(Items.RED_MUSHROOM)),
                new ItemStorage(new ItemStack(Items.BOWL))))
            .result(new ItemStack(Items.RABBIT_STEW))
            .minBuildingLevel(3)
            .build(consumer);
    }
}
