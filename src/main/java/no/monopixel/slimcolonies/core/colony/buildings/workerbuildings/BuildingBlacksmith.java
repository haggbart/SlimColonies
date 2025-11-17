package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.api.util.constant.TagConstants.CRAFTING_BLACKSMITH;

/**
 * Creates a new building for the blacksmith.
 */
public class BuildingBlacksmith extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BLACKSMITH = "blacksmith";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingBlacksmith(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_BLACKSMITH)
                .combine(super.getIngredientValidator());
        }

        /**
         * Check if a tag is a forge component that blacksmith can use as input ingredient.
         */
        private static boolean isBlacksmithInputComponent(final net.minecraft.tags.TagKey<Item> tag)
        {
            if (!tag.location().getNamespace().equals("forge"))
            {
                return false;
            }
            final String path = tag.location().getPath();
            return path.equals("plates") || path.equals("rings");
        }

        /**
         * Check if a tag is a forge component that blacksmith can craft as output.
         */
        private static boolean isBlacksmithOutputComponent(final net.minecraft.tags.TagKey<Item> tag)
        {
            if (!tag.location().getNamespace().equals("forge"))
            {
                return false;
            }
            final String path = tag.location().getPath();
            return path.equals("screws") ||
                path.equals("rings") ||
                path.equals("bolts") ||
                path.equals("rods") ||
                path.equals("rods/long") ||
                path.equals("dusts");
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }

            if (recipe.matchesInput(OptionalPredicate.failIf(input -> input.is(Items.LEATHER)))
                .equals(Optional.of(false)))
            {
                // explicitly disallow anything using leather; that's the fletcher's responsibility
                return false;
            }
            if (recipe.matchesInput(OptionalPredicate.passIf(input ->
                    input.getTags().anyMatch(CraftingModule::isBlacksmithInputComponent)))
                .equals(Optional.of(true)))
            {
                return true;
            }
            if (recipe.matchesOutput(OptionalPredicate.passIf(output ->
                    output.getTags().anyMatch(CraftingModule::isBlacksmithOutputComponent)))
                .equals(Optional.of(true)))
            {
                return true;
            }
            if (recipe.matchesOutput(OptionalPredicate.passIf(output ->
                        ModEquipmentTypes.axe.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.pickaxe.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.shovel.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.hoe.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.shears.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.sword.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.shield.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.helmet.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.chestplate.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.leggings.get().checkIsEquipment(output) ||
                            ModEquipmentTypes.boots.get().checkIsEquipment(output)
                    // deliberately excluding FISHINGROD and FLINT_N_STEEL
                ))
                .equals(Optional.of(true)))
            {
                // allow any other tool/armor even if it uses an excluded ingredient
                return true;
            }

            // otherwise obey the usual tags for other items
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_BLACKSMITH).orElse(false);
        }
    }
}
