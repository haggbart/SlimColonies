package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractAISkeleton;
import net.minecraft.nbt.CompoundTag;

/**
 * Common job object for all structure AIs.
 * Work order management has been moved to AbstractBuildingStructureBuilder.
 */
public abstract class AbstractJobStructure<AI extends AbstractAISkeleton<J>, J extends AbstractJobStructure<AI, J>> extends AbstractJob<AI, J>
{
    /**
     * Tag to store the workOrder id (for backwards compatibility during migration).
     */
    public static final String TAG_WORK_ORDER = "workorder";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobStructure(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        // Migration: if we find old work order data, move it to the building
        if (compound.contains(TAG_WORK_ORDER) && workBuilding instanceof AbstractBuildingStructureBuilder abstractBuildingStructureBuilder)
        {
            abstractBuildingStructureBuilder.setWorkOrderId(compound.getInt(TAG_WORK_ORDER));
        }
    }
}
