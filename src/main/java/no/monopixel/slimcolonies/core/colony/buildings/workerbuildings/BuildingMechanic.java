package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.HopperBlock;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractDOCraftingBuildingModule;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.api.util.constant.TagConstants.*;

/**
 * Class of the mechanic building.
 */
public class BuildingMechanic extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String MECHANIC = "mechanic";

    /**
     * Instantiates a new mechanic building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingMechanic(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return MECHANIC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    /**
     * Mechanic crafting module.
     */
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_MECHANIC)
                .combine(super.getIngredientValidator());
        }

        /**
         * Check if an item is a GregTech wire or cable based on its registry ID.
         * Wires/cables don't have reliable item tags, so we check the item ID directly.
         */
        private static boolean isGregTechWireOrCable(final ItemStack stack)
        {
            final net.minecraft.resources.ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null || !itemId.getNamespace().equals("gtceu"))
            {
                return false;
            }

            final String path = itemId.getPath();
            return path.endsWith("_wire") || path.endsWith("_cable");
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }

            if (recipe.matchesInput(OptionalPredicate.passIf(CraftingModule::isGregTechWireOrCable))
                .equals(Optional.of(true)))
            {
                return true;
            }
            if (recipe.matchesOutput(OptionalPredicate.passIf(CraftingModule::isGregTechWireOrCable))
                .equals(Optional.of(true)))
            {
                return true;
            }

            final Item item = recipe.getPrimaryOutput().getItem();
            if (item instanceof MinecartItem
                || (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof HopperBlock))
            {
                return true;
            }

            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_MECHANIC).orElse(false);
        }
    }

    public static class DOCraftingModule extends AbstractDOCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public DOCraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        /**
         * See {@link ICraftingBuildingModule#getIngredientValidator}.
         *
         * @return the validator
         */
        public @NotNull
        static OptionalPredicate<ItemStack> getStaticIngredientValidator()
        {
            final OptionalPredicate<ItemStack> sawmill = CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_SAWMILL, true)
                .combine(stack -> Optional.of(stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS)));

            final Predicate<ItemStack> handled = sawmill
                .or(CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_FLETCHER, true))
                .or(CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_STONEMASON, true))
                .or(CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_GLASSBLOWER, true))
                .orElse(false);

            // mechanic accepts every ingredient not otherwise handled
            return OptionalPredicate.of(handled.negate());
        }

        @Override
        public @NotNull OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return getStaticIngredientValidator();
        }
    }
}
