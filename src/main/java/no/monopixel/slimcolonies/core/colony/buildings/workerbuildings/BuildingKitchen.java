package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.FoodUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.util.FurnaceRecipes;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobCrafter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static no.monopixel.slimcolonies.api.util.constant.TagConstants.CRAFTING_COOK;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.CHEF_WORK;

/**
 * Class of the kitchen building.
 */

public class BuildingKitchen extends AbstractBuilding
{
    /**
     * The cook string.
     */
    private static final String KITCHEN_DESC = "kitchen";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingKitchen(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return KITCHEN_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        final ICitizenData citizenData = getModule(CHEF_WORK).getFirstCitizen();
        if (citizenData != null)
        {
            final IRequest<? extends IRequestable> currentTask = ((AbstractJobCrafter<?, ?>) citizenData.getJob()).getCurrentTask();
            if (currentTask == null)
            {
                return super.canEat(stack);
            }
            final IRequestable request = currentTask.getRequest();
            if (request instanceof AbstractCrafting craftingRequest)
            {
                final IRecipeStorage recipe = IColonyManager.getInstance().getRecipeManager().getRecipe(craftingRequest.getRecipeID());
                if (recipe != null)
                {
                    if (recipe.getCleanedInput().contains(new ItemStorage(stack)))
                    {
                        return false;
                    }

                    if (ItemStack.isSameItem(recipe.getPrimaryOutput(), stack))
                    {
                        return false;
                    }
                }
            }
        }
        return super.canEat(stack);
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            final ItemStack output = recipe.getPrimaryOutput();
            return FoodUtils.EDIBLE.test(output)
                || FoodUtils.EDIBLE.test(FurnaceRecipes.getInstance()
                    .getSmeltingResult(output));
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK).orElse(FoodUtils.EDIBLE.test(recipe.getPrimaryOutput()));
        }
    }
}
