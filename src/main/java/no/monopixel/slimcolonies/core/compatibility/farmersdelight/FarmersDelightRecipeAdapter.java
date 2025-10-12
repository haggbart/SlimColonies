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
 * Dynamically discovers and converts Farmer's Delight cooking pot recipes to SlimColonies chef recipes.
 * Works without code dependencies by checking mod presence at runtime.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FarmersDelightRecipeAdapter
{
    private static final String           FARMERS_DELIGHT_MOD_ID = "farmersdelight";
    private static final String           CHEF_CRAFTER           = ModJobs.CHEF_ID.getPath() + "_crafting";  // "chef_crafting"
    private static final ResourceLocation FD_COOKING_RECIPE_TYPE = new ResourceLocation(FARMERS_DELIGHT_MOD_ID, "cooking");
    private static final ResourceLocation FD_CUTTING_RECIPE_TYPE = new ResourceLocation(FARMERS_DELIGHT_MOD_ID, "cutting");

    /**
     * Listen for custom recipes being reloaded, then discover and convert FD recipes.
     */
    @SubscribeEvent
    public static void onCustomRecipesReloaded(@NotNull final CustomRecipesReloadedEvent event)
    {
        if (!ModList.get().isLoaded(FARMERS_DELIGHT_MOD_ID))
        {
            Log.getLogger().info("Farmer's Delight not installed, skipping recipe discovery");
            return;
        }

        Log.getLogger().info("Farmer's Delight detected, starting recipe discovery...");

        try
        {
            discoverAndConvertRecipes();
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error discovering Farmer's Delight recipes", e);
        }
    }

    /**
     * Discovers Farmer's Delight cooking recipes and converts them for the chef.
     */
    private static void discoverAndConvertRecipes()
    {
        // Get the recipe manager from the level
        final RecipeManager recipeManager = getRecipeManager();
        if (recipeManager == null)
        {
            Log.getLogger().warn("Recipe manager not available, cannot discover FD recipes");
            return;
        }

        Log.getLogger().info("Scanning {} total recipes for Farmer's Delight recipes...", recipeManager.getRecipes().size());

        int convertedCount = 0;
        int skippedCount = 0;
        int fdRecipeCount = 0;

        // Scan all recipes looking for Farmer's Delight cooking and cutting recipes
        for (final Recipe<?> recipe : recipeManager.getRecipes())
        {
            final String recipeTypeStr = recipe.getType().toString();

            // Check if this is a Farmer's Delight cooking or cutting recipe
            if (!recipeTypeStr.equals(FD_COOKING_RECIPE_TYPE.toString()) &&
                !recipeTypeStr.equals(FD_CUTTING_RECIPE_TYPE.toString()))
            {
                continue;
            }

            fdRecipeCount++;
            final String recipeType = recipeTypeStr.contains("cutting") ? "cutting" : "cooking";
            Log.getLogger().info("Found FD {} recipe: {}", recipeType, recipe.getId());

            // Debug: Log all recipe data
            Log.getLogger().info("  Recipe class: {}", recipe.getClass().getName());
            Log.getLogger().info("  Output: {}", recipe.getResultItem(null));
            Log.getLogger().info("  Ingredients count: {}", recipe.getIngredients().size());
            for (int i = 0; i < recipe.getIngredients().size(); i++)
            {
                final Ingredient ing = recipe.getIngredients().get(i);
                final ItemStack[] stacks = ing.getItems();
                if (stacks.length > 0)
                {
                    Log.getLogger().info("    Ingredient {}: {} (options: {})", i, stacks[0], stacks.length);
                }
            }

            // Try to find container field using reflection
            try
            {
                final java.lang.reflect.Method getOutputContainer = recipe.getClass().getMethod("getOutputContainer");
                final ItemStack container = (ItemStack) getOutputContainer.invoke(recipe);
                Log.getLogger().info("  Output container (via getOutputContainer): {}", container);
            }
            catch (final Exception e)
            {
                Log.getLogger().info("  No getOutputContainer method");
            }

            try
            {
                final java.lang.reflect.Field containerField = recipe.getClass().getDeclaredField("outputContainer");
                containerField.setAccessible(true);
                final ItemStack container = (ItemStack) containerField.get(recipe);
                Log.getLogger().info("  Output container (via field): {}", container);
            }
            catch (final Exception e)
            {
                Log.getLogger().info("  No outputContainer field");
            }

            try
            {
                // Try to convert the recipe
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

        Log.getLogger().info("Found {} Farmer's Delight recipes total (cooking + cutting)", fdRecipeCount);
        if (convertedCount > 0 || skippedCount > 0)
        {
            Log.getLogger().info("Converted {} Farmer's Delight recipes for chef ({} skipped due to incompatible ingredients)",
                convertedCount, skippedCount);
        }

        // Verify recipes are actually in the manager
        final int totalChefRecipes = CustomRecipeManager.getInstance().getRecipes(CHEF_CRAFTER).size();
        Log.getLogger().info("Total chef recipes in CustomRecipeManager: {}", totalChefRecipes);
    }

    /**
     * Converts a single Farmer's Delight recipe to a SlimColonies chef recipe.
     *
     * @param fdRecipe the Farmer's Delight recipe
     * @return true if the recipe was converted and added, false if skipped
     */
    private static boolean convertRecipe(@NotNull final Recipe<?> fdRecipe)
    {
        // Extract ingredients from the recipe
        final List<ItemStorage> inputs = new ArrayList<>();
        final List<String> incompatibleIngredients = new ArrayList<>();

        for (final Ingredient ingredient : fdRecipe.getIngredients())
        {
            // Get the first matching item from the ingredient
            final ItemStack[] matchingStacks = ingredient.getItems();
            if (matchingStacks.length == 0)
            {
                continue;
            }

            final ItemStack stack = matchingStacks[0];

            // Check if this ingredient is allowed for the chef
            if (!isChefIngredient(stack))
            {
                // Track incompatible ingredient for logging
                incompatibleIngredients.add(stack.getItem().toString());
                Log.getLogger().info("Recipe {} skipped - incompatible ingredient: {}", fdRecipe.getId(), stack.getItem());
                return false;
            }

            inputs.add(new ItemStorage(stack));
        }

        // Get the output
        final ItemStack output = fdRecipe.getResultItem(null);
        if (output.isEmpty())
        {
            return false;
        }

        // Check if the output is excluded from chef products (e.g., dyes, cakes)
        if (output.is(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_COOK)))
        {
            Log.getLogger().info("Recipe {} skipped - output is excluded: {}", fdRecipe.getId(), output.getItem());
            return false;
        }

        // Try to get the output container (bowl, glass bottle, pumpkin, etc.) using reflection
        try
        {
            final java.lang.reflect.Method getOutputContainer = fdRecipe.getClass().getMethod("getOutputContainer");
            final ItemStack container = (ItemStack) getOutputContainer.invoke(fdRecipe);

            if (!container.isEmpty())
            {
                // Accept all containers - if FD specifies it, the chef can use it
                // The ingredient filtering already handles food safety via CRAFTING_COOK tag
                inputs.add(new ItemStorage(container));
                Log.getLogger().debug("Added container {} to recipe {}", container, fdRecipe.getId());
            }
        }
        catch (final Exception e)
        {
            // No container - that's okay, not all recipes need one
        }

        // Create a unique recipe ID
        final ResourceLocation recipeId = new ResourceLocation(
            "slimcolonies",
            "farmersdelight_compat/" + CHEF_CRAFTER + "/" + fdRecipe.getId().getPath()
        );

        final CustomRecipe customRecipe = new CustomRecipe(
            CHEF_CRAFTER,                      // crafter
            1,                                  // minBldgLevel
            5,                                  // maxBldgLevel
            false,                              // mustExist
            false,                              // showTooltip
            recipeId,                           // recipeId
            Collections.emptySet(),             // researchReqs
            Collections.emptySet(),             // researchExcludes
            null,                               // lootTable
            ModEquipmentTypes.none.get(),      // requiredTool
            inputs,                             // inputs
            output.copy(),                      // primaryOutput
            Collections.emptyList(),            // secondaryOutput
            Collections.emptyList(),            // altOutputs
            Blocks.AIR                          // intermediate (no cooking pot needed!)
        );

        // Add to the custom recipe manager
        CustomRecipeManager.getInstance().addRecipe(customRecipe);

        Log.getLogger().debug("Converted FD recipe: {} -> {}", fdRecipe.getId(), output.getDisplayName().getString());
        return true;
    }

    /**
     * Checks if an item is allowed as a chef ingredient based on tags.
     *
     * @param stack the item stack to check
     * @return true if the chef can use this ingredient
     */
    private static boolean isChefIngredient(@NotNull final ItemStack stack)
    {
        // Allow if in the cook ingredient tag
        if (stack.is(ModTags.crafterIngredient.get(TagConstants.CRAFTING_COOK)))
        {
            return true;
        }

        // Also allow any item from the farmersdelight mod
        final String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return itemId.startsWith("farmersdelight:");
    }

    /**
     * Gets the recipe manager from the server.
     * This event always fires server-side, so we only need to check the server.
     *
     * @return the recipe manager, or null if not available
     */
    @Nullable
    private static RecipeManager getRecipeManager()
    {
        final var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getRecipeManager() : null;
    }
}
