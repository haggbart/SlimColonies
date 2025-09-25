package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.compatibility.ICompatibilityManager;
import no.monopixel.slimcolonies.api.crafting.*;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import no.monopixel.slimcolonies.core.colony.crafting.CustomRecipe;
import no.monopixel.slimcolonies.core.util.FurnaceRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.monopixel.slimcolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the smeltery building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingSmeltery extends AbstractBuilding
{
    /**
     * The smelter string.
     */
    private static final String SMELTERY_DESC = "smeltery";

    /**
     * Max building level of the smeltery.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Amount of swords and armor to keep at the worker.
     */
    private static final int STUFF_TO_KEEP = 10;

    /**
     * Instantiates a new smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(IColonyManager.getInstance().getCompatibilityManager()::isOre, new Tuple<>(Integer.MAX_VALUE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack)
                && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof DiggerItem || stack.getItem() instanceof ArmorItem)
            , new Tuple<>(STUFF_TO_KEEP, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SMELTERY_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
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

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            // all "recipes" are handled by the AI, and queried via the job
            return false;
        }

        @Override
        public boolean isVisible()
        {
            return false;
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            final ICompatibilityManager compatibility = IColonyManager.getInstance().getCompatibilityManager();
            for (final ItemStack stack : compatibility.getListOfAllItems())
            {
                if (ItemStackUtils.IS_SMELTABLE.and(compatibility::isOre).and(s -> !s.is(ModTags.breakable_ore)).test(stack))
                {
                    final ItemStack output = FurnaceRecipes.getInstance().getSmeltingResult(stack);
                    recipes.add(createSmeltingRecipe(stack, output, Blocks.FURNACE));
                }
            }
            return recipes;
        }

        private static IGenericRecipe createSmeltingRecipe(final ItemStack input, final ItemStack output, final Block intermediate)
        {
            return GenericRecipe.builder()
                .withInputs(List.of(List.of(input)))
                .withOutput(output)
                .withIntermediate(intermediate)
                .build();
        }
    }

    public static class OreBreakingModule extends AbstractCraftingBuildingModule.Custom
    {
        public OreBreakingModule(JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public List<ResourceLocation> getAdditionalLootTables()
        {
            final List<ResourceLocation> lootTables = new ArrayList<>(super.getAdditionalLootTables());

            //noinspection ConstantConditions
            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                lootTables.add(getLootTable(input));
            }

            return lootTables;
        }

        @Override
        protected boolean isPreTaughtRecipe(final IRecipeStorage storage, final Map<ResourceLocation, CustomRecipe> crafterRecipes)
        {
            if (storage.getPrimaryOutput().isEmpty() && storage.getLootTable() != null)
            {
                return true;
            }

            return super.isPreTaughtRecipe(storage, crafterRecipes);
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            //noinspection ConstantConditions
            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                recipes.add(GenericRecipe.builder()
                    .withInputs(List.of(List.of(input.getDefaultInstance())))
                    .withLootTable(getLootTable(input))
                    .build());
            }

            return recipes;
        }

        @Override
        public void checkForWorkerSpecificRecipes()
        {
            super.checkForWorkerSpecificRecipes();

            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                Block b = Block.byItem(input);
                List<ItemStack> drops = Block.getDrops(b.defaultBlockState(), (ServerLevel) building.getColony().getWorld(), building.getID(), null);
                for (ItemStack drop : drops)
                {
                    if (!drop.isEmpty())
                    {
                        drop.setCount(1);
                    }
                }

                final RecipeStorage tempRecipe = RecipeStorage.builder()
                    .withInputs(Collections.singletonList(new ItemStorage(new ItemStack(input))))
                    .withSecondaryOutputs(drops)
                    .withLootTable(getLootTable(input))
                    .build();
                IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(tempRecipe);
                this.addRecipeToList(token, false);
            }
        }

        @Override
        public ItemStack getCraftingTool(final AbstractEntityCitizen worker)
        {
            ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
            int fortuneLevel = building.getBuildingLevel() - 1;
            if (fortuneLevel > 0)
            {
                pick.enchant(Enchantments.BLOCK_FORTUNE, fortuneLevel);
            }
            return pick;
        }

        protected ResourceLocation getLootTable(Item item)
        {
            if (item instanceof BlockItem)
            {
                Block itemBlock = Block.byItem(item);
                return itemBlock.getLootTable();
            }
            return null;
        }
    }
}
