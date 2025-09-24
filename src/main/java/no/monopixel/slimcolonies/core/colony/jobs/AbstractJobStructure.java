package no.monopixel.slimcolonies.core.colony.jobs;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.workorders.IBuilderWorkOrder;
import no.monopixel.slimcolonies.api.colony.workorders.IWorkOrder;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.Utils;
import no.monopixel.slimcolonies.api.util.constant.NbtTagConstants;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractAISkeleton;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Common job object for all structure AIs.
 */
public abstract class AbstractJobStructure<AI extends AbstractAISkeleton<J>, J extends AbstractJobStructure<AI, J>> extends AbstractJob<AI, J>
{
    /**
     * Tag to store the workOrder id.
     */
    private static final String TAG_WORK_ORDER = "workorder";

    /**
     * The id of the current workOrder.
     */
    private int workOrderId;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobStructure(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Get the Work Order ID for this Job.
     *
     * @return UUID of the Work Order claimed by this Job, or null
     */
    public int getWorkOrderId()
    {
        return workOrderId;
    }

    /**
     * Does this job have a Work Order it has claimed?
     *
     * @return true if there is a Work Order claimed by this Job
     */
    public boolean hasWorkOrder()
    {
        if (workOrderId == 0 || getWorkOrder() == null)
        {
            workOrderId = 0;
            return false;
        }
        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        if (workOrderId != 0)
        {
            compound.putInt(TAG_WORK_ORDER, workOrderId);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInt(TAG_WORK_ORDER);
        }
    }

    /**
     * Do final completion when the Job's current work is complete.
     */
    public void complete()
    {
        getWorkOrder().onCompleted(getCitizen().getColony(), this.getCitizen());

        final Blueprint blueprint = getWorkOrder().getBlueprint();
        if (blueprint != null)
        {
            final CompoundTag[][][] tileEntityData = blueprint.getTileEntities();
            for (short x = 0; x < blueprint.getSizeX(); x++)
            {
                for (short y = 0; y < blueprint.getSizeY(); y++)
                {
                    for (short z = 0; z < blueprint.getSizeZ(); z++)
                    {
                        final CompoundTag compoundNBT = tileEntityData[y][z][x];
                        if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
                        {
                            final BlockPos tePos = getWorkOrder().getLocation().subtract(blueprint.getPrimaryBlockOffset()).offset(x, y, z);
                            final BlockEntity te = getColony().getWorld().getBlockEntity(tePos);
                            if (te instanceof IBlueprintDataProviderBE)
                            {
                                final CompoundTag tagData = compoundNBT.getCompound(TAG_BLUEPRINTDATA);
                                final String schematicPath = tagData.getString(TAG_NAME);
                                final String location = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(Utils.resolvePath(blueprint.getFilePath(), schematicPath));

                                tagData.putString(TAG_NAME, location);
                                tagData.putString(NbtTagConstants.TAG_PACK, blueprint.getPackName());

                                try
                                {
                                    ((IBlueprintDataProviderBE) te).readSchematicDataFromNBT(compoundNBT);
                                }
                                catch (final Exception e)
                                {
                                    Log.getLogger().warn("Broken deco-controller at: " + x + " " + y + " " + z);
                                }
                                ((ServerLevel) getColony().getWorld()).getChunkSource().blockChanged(tePos);
                                te.setChanged();
                            }
                        }
                    }
                }
            }
        }

        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
    }

    /**
     * Get the Work Order for the Job. Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuildDecoration for the Build
     */
    public IBuilderWorkOrder getWorkOrder()
    {
        final @Nullable IBuilderWorkOrder workOrder = getColony().getWorkManager().getWorkOrder(workOrderId, IBuilderWorkOrder.class);
        if (workOrder == null)
        {
            return null;
        }
        else if (!workOrder.getClaimedBy().equals(getCitizen().getWorkBuilding().getID()))
        {
            workOrderId = 0;
            return null;
        }
        return workOrder;
    }

    /**
     * Reset the needed items list.
     */
    private void resetNeededItems()
    {
        final IBuilding workerBuilding = this.getCitizen().getWorkBuilding();
        if (workerBuilding instanceof AbstractBuildingStructureBuilder)
        {
            ((AbstractBuildingStructureBuilder) workerBuilding).resetNeededResources();
        }
    }

    /**
     * Set a Work Order for this Job.
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(@Nullable final IWorkOrder order)
    {
        if (order == null)
        {
            workOrderId = 0;
            resetNeededItems();
        }
        else
        {
            workOrderId = order.getID();
        }
    }
}
