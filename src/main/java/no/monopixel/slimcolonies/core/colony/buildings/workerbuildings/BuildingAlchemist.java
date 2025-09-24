package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.crafting.GenericRecipe;
import no.monopixel.slimcolonies.api.crafting.IGenericRecipe;
import no.monopixel.slimcolonies.api.crafting.registry.CraftingType;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class of the alchemist building. Crafts potions and grows netherwart.
 */
public class BuildingAlchemist extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String ALCHEMIST = "alchemist";

    /**
     * List of soul sand blocks to grow onto.
     */
    private final List<BlockPos> soulsand = new ArrayList<>();

    /**
     * List of leave blocks to gather mistletoes from.
     */
    private final List<BlockPos> leaves = new ArrayList<>();

    /**
     * List of brewing stands.
     */
    private final List<BlockPos> brewingStands = new ArrayList<>();

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingAlchemist(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shears.get()), new Tuple<>(1, true));
        keepX.put(itemStack -> itemStack.getItem() == Items.NETHER_WART, new Tuple<>(16, false));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return ALCHEMIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == Blocks.SOUL_SAND)
        {
            soulsand.add(pos);
        }
        else if (block.is(BlockTags.LEAVES))
        {
            leaves.add(pos);
        }
        else if (block.getBlock() == Blocks.BREWING_STAND)
        {
            brewingStands.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag sandPos = compound.getList(TAG_PLANTGROUND, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            soulsand.add(NbtUtils.readBlockPos(sandPos.getCompound(i).getCompound(TAG_POS)));
        }

        final ListTag leavesPos = compound.getList(TAG_LEAVES, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < leavesPos.size(); ++i)
        {
            leaves.add(NbtUtils.readBlockPos(leavesPos.getCompound(i).getCompound(TAG_POS)));
        }

        final ListTag brewingStandPos = compound.getList(TAG_BREWING_STAND, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < brewingStandPos.size(); ++i)
        {
            brewingStands.add(NbtUtils.readBlockPos(brewingStandPos.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final ListTag sandCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : soulsand)
        {
            @NotNull final CompoundTag sandCompound = new CompoundTag();
            sandCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);

        @NotNull final ListTag leavesCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : leaves)
        {
            @NotNull final CompoundTag leaveCompound = new CompoundTag();
            leaveCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            leavesCompoundList.add(leaveCompound);
        }
        compound.put(TAG_LEAVES, leavesCompoundList);

        @NotNull final ListTag brewingStandCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : brewingStands)
        {
            @NotNull final CompoundTag brewingStandCompound = new CompoundTag();
            brewingStandCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            brewingStandCompoundList.add(brewingStandCompound);
        }
        compound.put(TAG_BREWING_STAND, brewingStandCompoundList);

        return compound;
    }

    /**
     * Get a list of all the available working positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllSoilPositions()
    {
        return new ArrayList<>(soulsand);
    }

    /**
     * Get a list of all leave positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllLeavePositions()
    {
        return new ArrayList<>(leaves);
    }

    /**
     * Get a list of all brewing stand positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllBrewingStandPositions()
    {
        return new ArrayList<>(brewingStands);
    }

    /**
     * Remove a vanished brewing stand.
     *
     * @param pos the position of it.
     */
    public void removeBrewingStand(final BlockPos pos)
    {
        brewingStands.remove(pos);
    }

    /**
     * Remove soil position.
     *
     * @param pos the position of it.
     */
    public void removeSoilPosition(final BlockPos pos)
    {
        soulsand.remove(pos);
    }

    /**
     * Remove leaf position.
     *
     * @param pos the position of it.
     */
    public void removeLeafPosition(final BlockPos pos)
    {
        leaves.remove(pos);
    }

    public static class BrewingModule extends AbstractCraftingBuildingModule.Brewing
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public BrewingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
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

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }

            return false; // Magic potion recipe removed with druid system
        }

        @Override
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Collections.emptySet();
        }

        @Override
        public @NotNull List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            // growing netherwart
            recipes.add(GenericRecipe.builder()
                .withOutput(Items.NETHER_WART, 4)
                .withInputs(List.of(List.of(Items.NETHER_WART.getDefaultInstance())))
                .withIntermediate(Blocks.SOUL_SAND)
                .build());

            return recipes;
        }
    }
}
