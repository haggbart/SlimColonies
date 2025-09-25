package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Planter
 */
public class DefaultPlanterCraftingProvider extends CustomRecipeProvider
{
    private static final String PLANTER = ModJobs.PLANTER_ID.getPath();

    public DefaultPlanterCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultPlanterCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
    }
}
