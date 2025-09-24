package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Fletcher
 */
public class DefaultFletcherCraftingProvider extends CustomRecipeProvider
{
    private static final String FLETCHER = ModJobs.FLETCHER_ID.getPath();

    public DefaultFletcherCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultFletcherCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        CustomRecipeBuilder.create(FLETCHER, MODULE_CRAFTING, "flint")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.GRAVEL), 3)))
                .result(new ItemStack(Items.FLINT))
                .build(consumer);

        CustomRecipeBuilder.create(FLETCHER, MODULE_CRAFTING, "string")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHITE_WOOL))))
                .result(new ItemStack(Items.STRING, 4))
                .build(consumer);
    }
}
