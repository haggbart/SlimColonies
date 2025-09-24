package no.monopixel.slimcolonies.core.generation.defaults.workers;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Alchemist
 */
public class DefaultAlchemistCraftingProvider extends CustomRecipeProvider
{
    private final String ALCHEMIST = ModJobs.ALCHEMIST_ID.getPath();

    public DefaultAlchemistCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultAlchemistCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        // Magic potion recipe removed - druid system removed from SlimColonies
    }
}
