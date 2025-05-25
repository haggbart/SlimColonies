package com.minecolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.client.gui.modules.FarmFieldsModuleWindow;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import com.minecolonies.core.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.core.items.ItemCrop;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_FARMER;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_FARMER_NO_SEED;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuilding
{
    /**
     * The beekeeper mode.
     */
    public static final ISettingKey<BoolSetting> FERTILIZE =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "fertilize"));

    /**
     * Descriptive string of the profession.
     */
    private static final String FARMER = "farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
    }

    @Override
    public boolean canBeGathered()
    {
        // Normal crafters are only gatherable when they have a task, i.e. while producing stuff.
        // BUT, the farmer both gathers and crafts things now, like the lumberjack
        return true;
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
            return building.getColony().getBuildingManager().getBuildingExtensions(field -> field.getBuildingExtensionType() == BuildingExtensionRegistries.farmField.get() && predicateToMatch.test(field));
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
                if (stack.getItem() instanceof ItemCrop cropItem && cropItem.getBlock() instanceof MinecoloniesCropBlock crop)
                {
                    // MineColonies crop
                    final TagKey<Biome> preferredBiome = crop.getPreferredBiome();
                    final Supplier<List<Component>> restrictions = preferredBiome == null ? ArrayList::new
                            : () -> provideBiomeList(preferredBiome);

                    recipes.add(GenericRecipe.builder()
                            .withInputs(List.of(List.of(cropItem.getDefaultInstance())))
                            .withIntermediate(crop.getPreferredFarmland())
                            .withLootTable(crop.getLootTable())
                            .withRequiredTool(ModEquipmentTypes.hoe.get())
                            .withRestrictions(restrictions)
                            .build());
                }
                else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
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
        private List<Component> provideBiomeList(@NotNull final TagKey<Biome> preferredBiome)
        {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return List.of();

            final Biome currentBiome = mc.level.getBiome(mc.player.blockPosition()).get();

            final Registry<Biome> biomeRegistry = mc.level.registryAccess().registryOrThrow(preferredBiome.registry());
            final Object[] biomes = biomeRegistry.getTag(preferredBiome).get().stream()
                    .map(b -> {
                        final MutableComponent name = Component.translatable(biomeRegistry.getKey(b.get()).toLanguageKey("biome"));
                        return b.get() == currentBiome ? name.withStyle(ChatFormatting.DARK_GREEN) : name;
                    })
                    .toArray();

            return List.of(Component.translatable(PARTIAL_JEI_INFO + "biomerestriction",
                    Component.translatable(String.join(", ", Collections.nCopies(biomes.length, "%s")), biomes)));
        }

        @NotNull
        @Override
        public List<ResourceLocation> getAdditionalLootTables()
        {
            final List<ResourceLocation> tables = new ArrayList<>(super.getAdditionalLootTables());
            for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (stack.getItem() instanceof ItemCrop cropItem && cropItem.getBlock() instanceof MinecoloniesCropBlock crop)
                {
                    tables.add(crop.getLootTable());
                }
                else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
                {
                    tables.add(crop.getLootTable());
                }
            }
            return tables;
        }
    }
}
