package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.IWareHouse;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IRequestable;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.constant.TranslationConstants;
import no.monopixel.slimcolonies.core.colony.Colony;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.WarehouseRequestQueueModule;
import no.monopixel.slimcolonies.core.colony.jobs.JobDeliveryman;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Resolver which resolves requests to it given deliverymen. Resolving is based on how well a request fits a dman, evaluated through request scores.
 */
public abstract class DeliverymenRequestResolver<R extends IRequestable> extends AbstractRequestResolver<R>
{
    public DeliverymenRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends R> requestToCheck)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return false;
        }

        if (manager.getColony().getBuildingManager().getBuilding(requestToCheck.getRequester().getLocation().getInDimensionLocation()) instanceof IWareHouse
              && !requestToCheck.getRequester().getLocation().equals(getLocation()))
        {
            return false;
        }

        return hasCouriers(manager);
    }

    /**
     * Get the deliverymen we can resolve requests for
     *
     * @param manager request manager
     * @return list of citizens
     */
    public boolean hasCouriers(@NotNull final IRequestManager manager)
    {
        final Colony colony = (Colony) manager.getColony();
        final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return false;
        }

       return !wareHouse.getModule(BuildingModules.WAREHOUSE_COURIERS).getAssignedCitizen().isEmpty();
    }

    @Override
    public int getSuitabilityMetric(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final IWareHouse wareHouse = manager.getColony().getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        final int distance = (int) BlockPosUtil.getDistance(request.getRequester().getLocation().getInDimensionLocation(), getLocation().getInDimensionLocation());
        if (wareHouse == null)
        {
            return distance;
        }
        return Math.max(distance/10, 1) + wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE).getMutableRequestList().size();
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        if (manager.getColony().getWorld().isClientSide || !hasCouriers(manager))
        {
            return null;
        }

        return Lists.newArrayList();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request) throws RuntimeException
    {
        final Colony colony = (Colony) manager.getColony();
        final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return;
        }

        if (wareHouse.getModule(BuildingModules.WAREHOUSE_COURIERS).getAssignedCitizen().isEmpty())
        {
            return;
        }

        final WarehouseRequestQueueModule module = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
        module.addRequest(request.getId());
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> completedRequest)
    {
        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            final Colony colony = (Colony) manager.getColony();
            final ICitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                                   .stream()
                                                   .filter(c -> c.getJob() instanceof JobDeliveryman && ((JobDeliveryman) c.getJob()).getTaskQueue().contains(request.getId()))
                                                   .findFirst()
                                                   .orElse(null);

            if (freeDeliveryMan != null)
            {
                final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
                job.onTaskDeletion(request.getId());
            }

            final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
            if (wareHouse == null)
            {
                return;
            }

            final WarehouseRequestQueueModule module = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
            module.getMutableRequestList().remove(request.getId());
        }
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN);
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
