package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AnimalHerdingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new building for the Chicken Herder.
 */
public class BuildingChickenHerder extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String JOB = "chickenherder";

    /**
     * The hut name, used for the lang string in the GUI
     */
    private static final String HUT_NAME = "chickenherderhut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingChickenHerder(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return JOB;
    }

    /**
     * Chicken herding module
     */
    public static class HerdingModule extends AnimalHerdingModule
    {
        public HerdingModule()
        {
            super(ModJobs.chickenHerder.get(), a -> a instanceof Chicken, new ItemStorage(Items.WHEAT_SEEDS, 2));
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getRecipesForDisplayPurposesOnly(@NotNull Animal animal)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getRecipesForDisplayPurposesOnly(animal));

            recipes.add(GenericRecipe.builder()
                    .withOutput(Items.EGG)
                    .withRequiredEntity(animal.getType())
                    .build());

            return recipes;
        }
    }
}
