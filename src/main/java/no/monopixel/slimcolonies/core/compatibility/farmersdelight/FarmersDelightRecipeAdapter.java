package no.monopixel.slimcolonies.core.compatibility.farmersdelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.eventbus.events.CustomRecipesReloadedEvent;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.util.constant.TagConstants;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipe;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Discovers and converts Farmer's Delight recipes for the chef without code dependencies.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FarmersDelightRecipeAdapter
{
    private static final String           FARMERS_DELIGHT_MOD_ID = "farmersdelight";
    private static final String           CHEF_CRAFTER           = ModJobs.CHEF_ID.getPath() + "_crafting";
    private static final ResourceLocation FD_COOKING_RECIPE_TYPE = new ResourceLocation(FARMERS_DELIGHT_MOD_ID, "cooking");
    private static final ResourceLocation FD_CUTTING_RECIPE_TYPE = new ResourceLocation(FARMERS_DELIGHT_MOD_ID, "cutting");

    @SubscribeEvent
    public static void onCustomRecipesReloaded(@NotNull final CustomRecipesReloadedEvent event)
    {
        if (!ModList.get().isLoaded(FARMERS_DELIGHT_MOD_ID))
        {
            return;
        }

        try
        {
            discoverAndConvertRecipes();
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error discovering Farmer's Delight recipes", e);
        }
    }

    private static void discoverAndConvertRecipes()
    {
        final RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null)
        {
            return;
        }

        int convertedCount = 0;
        int skippedCount = 0;

        for (final Recipe<?> recipe : recipeManager.getRecipes())
        {
            final String recipeTypeStr = recipe.getType().toString();

            if (!recipeTypeStr.equals(FD_COOKING_RECIPE_TYPE.toString()) &&
                !recipeTypeStr.equals(FD_CUTTING_RECIPE_TYPE.toString()))
            {
                continue;
            }

            try
            {
                if (convertRecipe(recipe))
                {
                    convertedCount++;
                }
                else
                {
                    skippedCount++;
                }
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Failed to convert FD recipe: " + recipe.getId(), e);
                skippedCount++;
            }
        }

        if (convertedCount > 0)
        {
            Log.getLogger().info("Converted {} Farmer's Delight recipes for chef ({} skipped)", convertedCount, skippedCount);
        }
    }

    private static boolean convertRecipe(@NotNull final Recipe<?> fdRecipe)
    {
        final List<ItemStorage> inputs = new ArrayList<>();

        for (final Ingredient ingredient : fdRecipe.getIngredients())
        {
            final ItemStack[] matchingStacks = ingredient.getItems();
            if (matchingStacks.length == 0)
            {
                continue;
            }

            final ItemStack stack = matchingStacks[0];
            if (!isChefIngredient(stack))
            {
                return false;
            }

            inputs.add(new ItemStorage(stack));
        }

        final ItemStack output = fdRecipe.getResultItem(null);
        if (output.isEmpty())
        {
            return false;
        }

        if (output.is(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_COOK)))
        {
            return false;
        }

        // Add container requirement if recipe has one (bowl, bottle, etc.)
        try
        {
            final java.lang.reflect.Method getOutputContainer = fdRecipe.getClass().getMethod("getOutputContainer");
            final ItemStack container = (ItemStack) getOutputContainer.invoke(fdRecipe);
            if (!container.isEmpty())
            {
                inputs.add(new ItemStorage(container));
            }
        }
        catch (final Exception ignored)
        {
        }

        final ResourceLocation recipeId = new ResourceLocation(
            "slimcolonies",
            "farmersdelight_compat/" + CHEF_CRAFTER + "/" + fdRecipe.getId().getPath()
        );

        final CustomRecipe customRecipe = new CustomRecipe(
            CHEF_CRAFTER,
            1,
            5,
            false,
            false,
            recipeId,
            Collections.emptySet(),
            Collections.emptySet(),
            null,
            ModEquipmentTypes.none.get(),
            inputs,
            output.copy(),
            Collections.emptyList(),
            Collections.emptyList(),
            Blocks.AIR
        );

        CustomRecipeManager.getInstance().addRecipe(customRecipe);
        return true;
    }

    private static boolean isChefIngredient(@NotNull final ItemStack stack)
    {
        if (stack.is(ModTags.crafterIngredient.get(TagConstants.CRAFTING_COOK)))
        {
            return true;
        }

        final String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return itemId.startsWith("farmersdelight:");
    }

    @Nullable
    private static RecipeManager getRecipeManager()
    {
        final var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getRecipeManager() : null;
    }
}
