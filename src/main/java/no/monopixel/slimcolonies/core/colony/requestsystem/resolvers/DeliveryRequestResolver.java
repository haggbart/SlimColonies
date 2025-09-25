package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import com.google.common.reflect.TypeToken;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.IWareHouse;
import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.manager.IRequestManager;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves deliveries
 */
public class DeliveryRequestResolver extends DeliverymenRequestResolver<Delivery>
{
    public DeliveryRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends Delivery> requestToCheck)
    {
        final IWareHouse wareHouse = manager.getColony().getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return false;
        }

        return super.canResolveRequest(manager, requestToCheck);
    }

    @Override
    public TypeToken<? extends Delivery> getRequestType()
    {
        return TypeConstants.DELIVERY;
    }

}
