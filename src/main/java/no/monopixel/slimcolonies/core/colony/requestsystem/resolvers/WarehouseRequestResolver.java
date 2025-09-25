package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import org.jetbrains.annotations.NotNull;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseRequestResolver extends AbstractWarehouseRequestResolver
{
    public WarehouseRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    protected int getWarehouseInternalCount(final BuildingWareHouse wareHouse, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (requestToCheck.getRequest() instanceof IConcreteDeliverable)
        {
            return 0;
        }

        return InventoryUtils.hasBuildingEnoughElseCount(wareHouse, itemStack -> requestToCheck.getRequest().matches(itemStack), requestToCheck.getRequest().getCount());
    }
}
