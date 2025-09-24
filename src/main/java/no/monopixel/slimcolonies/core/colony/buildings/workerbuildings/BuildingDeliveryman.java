package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.jobs.JobDeliveryman;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.COURIER_WORK;

/**
 * Class of the warehouse building.
 */
public class BuildingDeliveryman extends AbstractBuilding implements IBuildingDeliveryman
{

    private static final String DELIVERYMAN = "deliveryman";

    /**
     * Instantiates a new warehouse building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDeliveryman(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DELIVERYMAN;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        final ICitizenData citizenData = getModule(COURIER_WORK).getFirstCitizen();
        if (citizenData != null)
        {
            final JobDeliveryman job = (JobDeliveryman) citizenData.getJob();
            final IRequest<? extends IRequestable> currentTask = job.getCurrentTask();
            if (currentTask == null)
            {
                return super.canEat(stack);
            }
            final IRequestable request = currentTask.getRequest();
            if (request instanceof Delivery && ItemStack.isSameItem(((Delivery) request).getStack(), stack))
            {
                return false;
            }
        }
        return super.canEat(stack);
    }
}
