package no.monopixel.slimcolonies.core.colony.requestsystem.management.handlers.update.implementation;

import no.monopixel.slimcolonies.api.colony.requestsystem.management.update.UpdateType;
import no.monopixel.slimcolonies.core.colony.requestsystem.management.IStandardRequestManager;
import no.monopixel.slimcolonies.core.colony.requestsystem.management.handlers.update.IUpdateStep;
import org.jetbrains.annotations.NotNull;

/**
 * Update fix to restaurant.
 */
public class ResetRSToUpdateRestaurantResolver implements IUpdateStep
{
    @Override
    public int updatesToVersion()
    {
        return 14;
    }

    @Override
    public void update(@NotNull final UpdateType type, @NotNull final IStandardRequestManager manager)
    {
        if (type == UpdateType.DATA_LOAD)
        {
            manager.reset();
        }
    }
}
