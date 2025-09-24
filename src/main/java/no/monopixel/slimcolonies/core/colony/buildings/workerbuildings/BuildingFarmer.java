package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import no.monopixel.slimcolonies.api.colony.buildings.modules.settings.ISettingKey;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.CraftingUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.OptionalPredicate;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.client.gui.modules.FarmFieldsModuleWindow;
import no.monopixel.slimcolonies.core.colony.buildingextensions.FarmField;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.BoolSetting;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.SettingKey;
import no.monopixel.slimcolonies.core.colony.buildings.moduleviews.FieldsModuleView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.TagConstants.CRAFTING_FARMER;
import static no.monopixel.slimcolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_FARMER_NO_SEED;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuilding
{
    /**
     * The beekeeper mode.
     */
    public static final ISettingKey<BoolSetting> FERTILIZE =
        new SettingKey<>(BoolSetting.class, new ResourceLocation(Constants.MOD_ID, "fertilize"));

    /**
     * Descriptive string of the profession.
     */
    private static final String FARMER = "farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * The offset to work at relative to the scarecrow.
     */
    @Nullable
    private BlockPos workingOffset;

    /**
     * The previous position which has been worked at.
     */
    @Nullable
    private BlockPos prevPos;

    /**
     * The current index within the current field
     */
    private int cell = -1;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.hoe.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get()), new Tuple<>(1, true));
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a map of objects which should be kept.
     */
    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        for (BuildingExtensionsModule module : getModulesByType(BuildingExtensionsModule.class))
        {
            for (final IBuildingExtension field : module.getOwnedExtensions())
            {
                if (field instanceof FarmField farmField && !farmField.getSeed().isEmpty())
                {
                    toKeep.put(stack -> ItemStack.isSameItem(farmField.getSeed(), stack), new Tuple<>(64, true));
                }
            }
        }
        return toKeep;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        for (BuildingExtensionsModule module : getModulesByType(BuildingExtensionsModule.class))
        {
            for (final IBuildingExtension field : module.getOwnedExtensions())
            {
                if (field instanceof FarmField farmField && !farmField.getSeed().isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(farmField.getSeed(), stack))
                {
                    return false;
                }
            }
        }

        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    /**
     * Getter for request fertilizer
     */
    public boolean requestFertilizer()
    {
        return getSetting(FERTILIZE).getValue();
    }

    /**
     * Get the offset to work at relative to the scarecrow.
     *
     * @return the blockpos.
     */
    public BlockPos getWorkingOffset()
    {
        return workingOffset;
    }

    /**
     * Set the current index within the current field
     *
     * @param i the value to set.
     * @return current value.
     */
    public int setCell(final int i)
    {
        cell = i;
        return cell;
    }

    /**
     * Get the current index within the current field
     *
     * @return current value.
     */
    public int getCell()
    {
        return cell;
    }

    /**
     * Set the previous position which has been worked at.
     *
     * @param position to set.
     */
    public void setPrevPos(final BlockPos position)
    {
        this.prevPos = position;
    }

    /**
     * Set the offset to work at relative to the scarecrow.
     *
     * @param blockPos the pos to set.
     */
    public void setWorkingOffset(final BlockPos blockPos)
    {
        this.workingOffset = blockPos;
    }

    /**
     * Get the previous position which has been worked at.
     *
     * @return current prev pos.
     */
    public BlockPos getPrevPos()
    {
        return prevPos;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = super.serializeNBT();
        compoundTag.putInt(TAG_CELL, this.cell);
        if (workingOffset != null)
        {
            BlockPosUtil.write(compoundTag, TAG_WORKING_OFFSET, workingOffset);
        }
        if (prevPos != null)
        {
            BlockPosUtil.write(compoundTag, TAG_PREV_POS, prevPos);
        }
        return compoundTag;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_CELL))
        {
            this.cell = compound.getInt(TAG_CELL);
        }
        if (compound.contains(TAG_WORKING_OFFSET))
        {
            this.workingOffset = BlockPosUtil.read(compound, TAG_WORKING_OFFSET);
        }
        if (compound.contains(TAG_PREV_POS))
        {
            this.prevPos = BlockPosUtil.read(compound, TAG_PREV_POS);
        }
    }

    /**
     * Field module implementation for the farmer.
     */
    public static class FarmerFieldsModule extends BuildingExtensionsModule
    {
        @Override
        protected int getMaxExtensionCount()
        {
            return building.getBuildingLevel();
        }

        @Override
        public Class<?> getExpectedExtensionType()
        {
            return FarmField.class;
        }

        @Override
        public @NotNull List<IBuildingExtension> getMatchingExtension(final Predicate<IBuildingExtension> predicateToMatch)
        {
            return building.getColony()
                .getBuildingManager()
                .getBuildingExtensions(field -> field.getBuildingExtensionType() == BuildingExtensionRegistries.farmField.get() && predicateToMatch.test(field));
        }

        @Override
        public boolean canAssignExtensionOverride(final IBuildingExtension extension)
        {
            return extension instanceof FarmField farmField && !farmField.getSeed().isEmpty();
        }
    }

    /**
     * Field module view implementation for the farmer.
     */
    public static class FarmerFieldsModuleView extends FieldsModuleView
    {
        @Override
        @OnlyIn(Dist.CLIENT)
        public BOWindow getWindow()
        {
            return new FarmFieldsModuleWindow(buildingView, this);
        }

        @Override
        public boolean canAssignFieldOverride(final IBuildingExtension field)
        {
            return field instanceof FarmField farmField && !farmField.getSeed().isEmpty();
        }

        @Override
        protected List<IBuildingExtension> getFieldsInColony()
        {
            return getColony().getBuildingExtensions(field -> field.getBuildingExtensionType().equals(BuildingExtensionRegistries.farmField.get()));
        }

        @Override
        public @Nullable MutableComponent getFieldWarningTooltip(final IBuildingExtension field)
        {
            MutableComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (field instanceof FarmField farmField && farmField.getSeed().isEmpty())
            {
                return Component.translatable(FIELD_LIST_FARMER_NO_SEED);
            }

            return null;
        }
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_FARMER)
                .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_FARMER).orElse(false);
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull Level world)
        {
            List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));
            for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
                {
                    // regular crop
                    recipes.add(GenericRecipe.builder()
                        .withInputs(List.of(List.of(crop.getCloneItemStack(world, BlockPos.ZERO, crop.defaultBlockState()))))
                        .withIntermediate(Blocks.FARMLAND)
                        .withLootTable(crop.getLootTable())
                        .withRequiredTool(ModEquipmentTypes.hoe.get())
                        .build());
                }
                else if (stack.is(Tags.Items.SEEDS))
                {
                    // another kind of seed?
                    if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof StemBlock stem)
                    {
                        recipes.add(GenericRecipe.builder()
                            .withOutput(stem.getFruit())
                            .withInputs(List.of(List.of(stack)))
                            .withIntermediate(Blocks.FARMLAND)
                            .withRequiredTool(ModEquipmentTypes.hoe.get())
                            .build());
                    }
                    else
                    {
                        recipes.add(GenericRecipe.builder()
                            .withInputs(List.of(List.of(stack)))
                            .withIntermediate(Blocks.FARMLAND)
                            .withRequiredTool(ModEquipmentTypes.hoe.get())
                            .build());
                    }
                }
            }
            return recipes;
        }

        @NotNull
        @Override
        public List<ResourceLocation> getAdditionalLootTables()
        {
            final List<ResourceLocation> tables = new ArrayList<>(super.getAdditionalLootTables());
            for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
                {
                    tables.add(crop.getLootTable());
                }
            }
            return tables;
        }
    }
}
