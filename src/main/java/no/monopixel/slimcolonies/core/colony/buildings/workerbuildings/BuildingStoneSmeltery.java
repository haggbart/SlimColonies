package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.api.util.constant.TagConstants.CRAFTING_STONE_SMELTERY;

/**
 * Class of the stone smeltery building.
 */
public class BuildingStoneSmeltery extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String STONE_SMELTERY = "stonesmeltery";

    /**
     * Instantiates a new stone smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingStoneSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONE_SMELTERY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_STONE_SMELTERY)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_STONE_SMELTERY).orElse(false);
        }
    }
}
