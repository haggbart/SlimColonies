package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.colony.buildingextensions.plantation.IPlantationModule;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.core.client.gui.modules.PlantationFieldsModuleWindow;
import no.monopixel.slimcolonies.core.colony.buildingextensions.PlantationField;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.FieldsModuleView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
import static no.monopixel.slimcolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_PLANTATION_RESEARCH_REQUIRED;

/**
 * Class of the plantation building. Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuilding
{
    private static final String PLANTATION = "plantation";

    public BuildingPlantation(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shears.get()), new Tuple<>(1, true));
    }

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        updateFields();
    }

    private void updateFields()
    {
        updateField(BuildingExtensionRegistries.plantationSugarCaneField.get());
        updateField(BuildingExtensionRegistries.plantationCactusField.get());
        updateField(BuildingExtensionRegistries.plantationBambooField.get());
    }

    private void updateField(BuildingExtensionEntry type)
    {
        final PlantationField plantationField = PlantationField.create(type, getPosition());
        final List<BlockPos> workingPositions =
            plantationField.getModule().getValidWorkingPositions(colony.getWorld(), getLocationsFromTag(plantationField.getModule().getWorkTag()));
        if (workingPositions.isEmpty())
        {
            colony.getBuildingManager().removeBuildingExtension(field -> field.equals(plantationField));
            return;
        }

        if (colony.getBuildingManager().addBuildingExtension(plantationField))
        {
            plantationField.setWorkingPositions(workingPositions);
        }
        else
        {
            final Optional<IBuildingExtension> existingField = colony.getBuildingManager().getMatchingBuildingExtension(field -> field.equals(plantationField));
            if (existingField.isPresent() && existingField.get() instanceof PlantationField existingPlantationField)
            {
                existingPlantationField.setWorkingPositions(workingPositions);
            }
        }
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        updateFields();
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = super.getRequiredItemsAndAmount();
        for (BuildingExtensionsModule module : getModulesByType(BuildingExtensionsModule.class))
        {
            for (final IBuildingExtension field : module.getOwnedExtensions())
            {
                if (field instanceof PlantationField plantationField)
                {
                    final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                    toKeep.put(stack -> ItemStack.isSameItem(new ItemStack(plantationModule.getItem()), stack), new Tuple<>(plantationModule.getPlantsToRequest(), true));
                }
            }
        }
        return toKeep;
    }

    /**
     * Prevents workers from eating items being produced by assigned fields.
     */
    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (!super.canEat(stack))
        {
            return false;
        }

        for (BuildingExtensionsModule module : getModulesByType(BuildingExtensionsModule.class))
        {
            for (final IBuildingExtension field : module.getOwnedExtensions())
            {
                if (field instanceof PlantationField plantationField)
                {
                    final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(new ItemStack(plantationModule.getItem()), stack))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
    }

    public static class PlantationFieldsModule extends BuildingExtensionsModule
    {
        @Override
        protected int getMaxExtensionCount()
        {
            int allowedPlants = (int) Math.ceil(building.getBuildingLevel() / 2D);
            return allowedPlants + 1;
        }

        @Override
        public Class<?> getExpectedExtensionType()
        {
            return PlantationField.class;
        }

        @Override
        public @NotNull List<IBuildingExtension> getMatchingExtension(final Predicate<IBuildingExtension> predicateToMatch)
        {
            return building.getColony().getBuildingManager().getBuildingExtensions(field -> field.hasModule(IPlantationModule.class) && predicateToMatch.test(field));
        }

        @Override
        public boolean canAssignExtensionOverride(IBuildingExtension extension)
        {
            return hasRequiredResearchForField(extension);
        }

        private boolean hasRequiredResearchForField(final IBuildingExtension field)
        {
            if (field instanceof PlantationField plantationField)
            {
                final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                if (plantationModule.getRequiredResearchEffect() != null)
                {
                    return building.getColony().getResearchManager().getResearchEffects().getEffectStrength(plantationModule.getRequiredResearchEffect()) > 0;
                }
                return true;
            }
            return false;
        }
    }

    public static class PlantationFieldsModuleView extends FieldsModuleView
    {
        @Override
        protected boolean canAssignFieldOverride(final IBuildingExtension field)
        {
            return hasRequiredResearchForField(field);
        }

        @Override
        protected List<IBuildingExtension> getFieldsInColony()
        {
            return getColony().getBuildingExtensions(field -> field.hasModule(IPlantationModule.class));
        }

        @Override
        public @Nullable MutableComponent getFieldWarningTooltip(final IBuildingExtension field)
        {
            MutableComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (!hasRequiredResearchForField(field))
            {
                return Component.translatable(FIELD_LIST_PLANTATION_RESEARCH_REQUIRED);
            }
            return null;
        }

        private boolean hasRequiredResearchForField(final IBuildingExtension field)
        {
            if (field instanceof PlantationField plantationField)
            {
                final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                if (plantationModule.getRequiredResearchEffect() != null)
                {
                    return getColony().getResearchManager().getResearchEffects().getEffectStrength(plantationModule.getRequiredResearchEffect()) > 0;
                }
                return true;
            }
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public BOWindow getWindow()
        {
            return new PlantationFieldsModuleWindow(buildingView, this);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_PLANTATION);
            return isRecipeAllowed.orElse(false);
        }

        @Override
        public @NotNull List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            for (BuildingExtensionEntry type : BuildingExtensionRegistries.getBuildingExtensionRegistry().getValues())
            {
                type.getExtensionModuleProducers().stream()
                    .map(m -> m.apply(null))
                    .filter(IPlantationModule.class::isInstance)
                    .map(m -> (IPlantationModule) m)
                    .findFirst()
                    .ifPresent(module -> recipes.add(GenericRecipe.builder()
                        .withOutput(module.getItem())
                        .withInputs(List.of(module.getRequiredItemsForOperation()))
                        .withRequiredTool(module.getRequiredTool())
                        .build()));
            }

            return recipes;
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_PLANTATION).combine(super.getIngredientValidator());
        }
    }
}
