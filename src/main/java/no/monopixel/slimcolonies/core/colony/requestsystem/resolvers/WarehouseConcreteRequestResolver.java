package no.monopixel.slimcolonies.core.colony.requestsystem.resolvers;

import no.monopixel.slimcolonies.api.colony.requestsystem.location.ILocation;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.INonExhaustiveDeliverable;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.Stack;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import no.monopixel.slimcolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseConcreteRequestResolver extends AbstractWarehouseRequestResolver
{
    public WarehouseConcreteRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    protected int getWarehouseInternalCount(final BuildingWareHouse wareHouse, final IRequest<? extends IDeliverable> requestToCheck)
    {
        final IDeliverable deliverable = requestToCheck.getRequest();
        if (!(deliverable instanceof IConcreteDeliverable))
        {
            return 0;
        }

        boolean ignoreNBT = false;
        boolean ignoreDamage = false;
        if (deliverable instanceof Stack stack)
        {
            ignoreNBT = !stack.matchNBT();
            ignoreDamage = !stack.matchDamage();
        }
        int totalCount = 0;
        for (final ItemStack possible : ((IConcreteDeliverable) deliverable).getRequestedItems())
        {
            if (requestToCheck.getRequest() instanceof INonExhaustiveDeliverable neDeliverable)
            {
                totalCount += Math.max(0, InventoryUtils.hasBuildingEnoughElseCount(wareHouse,
                  new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getCount() + neDeliverable.getLeftOver()) - neDeliverable.getLeftOver());
            }
            else
            {
                totalCount += InventoryUtils.hasBuildingEnoughElseCount(wareHouse,
                  new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getCount());
            }

            if (totalCount >= requestToCheck.getRequest().getCount())
            {
                return totalCount;
            }
        }
        return totalCount;
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
