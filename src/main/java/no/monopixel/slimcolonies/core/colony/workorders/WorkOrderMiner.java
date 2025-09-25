package no.monopixel.slimcolonies.core.colony.workorders;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.workorders.IWorkManager;
import no.monopixel.slimcolonies.api.colony.workorders.WorkOrderType;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.ColonyUtils;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static no.monopixel.slimcolonies.api.util.constant.Constants.STORAGE_STYLE;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_POS;
/**
 * A work order that the build can take to build mine.
 */
public class WorkOrderMiner extends AbstractWorkOrder
{
    /**
     * Position of the issuer of the order.
     */
    private BlockPos minerBuilding;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderMiner()
    {
        super();
    }

    /**
     * Create a new work order telling the miner to build a mine.
     *
     * @param packName      The name of the pack.
     * @param structureName The path of the blueprint.
     * @param workOrderName The user friendly name of the mine.
     * @param rotation      The number of times the mine was rotated.
     * @param location      The location where the mine should be built.
     * @param mirror        Is the mine mirrored?
     * @param minerBuilding The id of the building of the miner.
     */
    public WorkOrderMiner(
      final String packName,
      final String structureName,
      final String workOrderName,
      final int rotation,
      final BlockPos location,
      final boolean mirror,
      final BlockPos minerBuilding)
    {
        super(packName, structureName, workOrderName, WorkOrderType.BUILD, location, rotation, mirror, 0, 1);
        this.minerBuilding = minerBuilding;
    }

    @Override
    public void loadBlueprint(final Level world, final Consumer<Blueprint> afterLoad)
    {
        if (blueprint != null)
        {
            afterLoad.accept(blueprint);
        }
        else if (future == null || future.isDone())
        {
            future = ColonyUtils.queueBlueprintLoad(world, getStructurePack(), getStructurePath(), blueprint ->
                {
                    setBlueprint(blueprint, world);
                    afterLoad.accept(blueprint);
                },
                error ->
                {
                    future = ColonyUtils.queueBlueprintLoad(world, STORAGE_STYLE, getStructurePath(), blueprint ->
                    {
                        setBlueprint(blueprint, world);
                        packName = STORAGE_STYLE;
                        afterLoad.accept(blueprint);
                    });
                });
        }
    }

    @Override
    public boolean canBuild(IBuilding building)
    {
        return building instanceof BuildingMiner && this.minerBuilding.equals(building.getID());
    }

    /**
     * Check if a citizen may accept this workOrder while ignoring the distance to the build location.
     * <p>
     * @param building    the assigned building.
     * @param position    the position of the citizen's work hut.
     * @param level       the level of that work hut.
     * @return true if the citizen may accept this work order.
     */
    
    @Override
    public boolean canBuildIgnoringDistance(@NotNull IBuilding building,final BlockPos position, final int level)
    {
        return canBuild(building);
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && colony.getBuildingManager().getBuilding(minerBuilding) != null;
    }

    /**
     * Read the WorkOrder data from the CompoundTag.
     *
     * @param compound NBT Tag compound.
     * @param manager  the work manager.
     */
    @Override
    public void read(@NotNull final CompoundTag compound, final IWorkManager manager)
    {
        super.read(compound, manager);
        minerBuilding = BlockPosUtil.read(compound, TAG_POS);
    }

    /**
     * Save the Work Order to an CompoundTag.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        super.write(compound);
        BlockPosUtil.write(compound, TAG_POS, minerBuilding);
    }

    /**
     * Get the miner building position assigned to this request.
     *
     * @return the BlockPos.
     */
    public BlockPos getMinerBuilding()
    {
        return minerBuilding;
    }
}
