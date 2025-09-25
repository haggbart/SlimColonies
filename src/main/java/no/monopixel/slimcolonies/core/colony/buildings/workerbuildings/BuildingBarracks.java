package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.NBTUtils;
import no.monopixel.slimcolonies.core.client.gui.huts.WindowBarracksBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_POS;


/**
 * Building class for the Barracks.
 */
public class BuildingBarracks extends AbstractBuilding
{
    /**
     * Name of our building's Schematics.
     */
    private static final String SCHEMATIC_NAME = "barracks";

    /**
     * Max hut level of the Barracks.
     */
    private static final int BARRACKS_HUT_MAX_LEVEL = 5;

    /**
     * The tag to store the tower list to NBT.
     */
    private static final String TAG_TOWERS = "towers";

    /**
     * The list of barracksTowers.
     */
    private final List<BlockPos> towers = new ArrayList<>();


    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    public BuildingBarracks(@NotNull final IColony colony, final BlockPos pos)
    {
        super(colony, pos);
        keepX.put((stack) -> stack.getItem() == Items.GOLD_INGOT, new Tuple<>(STACKSIZE, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    
    @Override
    public int getMaxBuildingLevel()
    {
        return BARRACKS_HUT_MAX_LEVEL;
    }

    @Override
    public void onDestroyed()
    {
        final Level world = getColony().getWorld();

        if (world != null)
        {
            for (final BlockPos tower : towers)
            {
                world.setBlockAndUpdate(tower, Blocks.AIR.defaultBlockState());
            }
        }
        super.onDestroyed();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == ModBlocks.blockHutBarracksTower)
        {
            final IBuilding building = getColony().getBuildingManager().getBuilding(pos);
            if (building instanceof BuildingBarracksTower)
            {
                building.setStructurePack(this.getStructurePack());
                ((BuildingBarracksTower) building).addBarracks(getPosition());
                if (!towers.contains(pos))
                {
                    towers.add(pos);
                }
            }
        }
    }


    @Override
    public int getClaimRadius(final int newLevel)
    {
        if (newLevel <= 0)
        {
            return 0;
        }

        // tower levels must all be 4+ to get increased radius of 3
        int barracksClaimRadius = 3;
        for (final BlockPos pos : towers)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                if (building.getBuildingLevel() < 4)
                {
                    barracksClaimRadius = 2;
                    break;
                }
            }
        }
        return barracksClaimRadius;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        towers.clear();
        towers.addAll(NBTUtils.streamCompound(compound.getList(TAG_TOWERS, Tag.TAG_COMPOUND))
                        .map(resultCompound -> BlockPosUtil.read(resultCompound, TAG_POS))
                        .collect(Collectors.toList()));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        final ListTag towerTagList = towers.stream().map(pos -> BlockPosUtil.write(new CompoundTag(), TAG_POS, pos)).collect(NBTUtils.toListNBT());
        compound.put(TAG_TOWERS, towerTagList);

        return compound;
    }

    public List<BlockPos> getTowers()
    {
        return towers;
    }

    /**
     * Barracks building View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the barracks view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowBarracksBuilding(this);
        }

        @Override
        public int getRange()
        {
            return getClaimRadius() * 16;
        }
    }
}
