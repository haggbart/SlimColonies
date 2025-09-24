package no.monopixel.slimcolonies.api.colony.managers.interfaces;

import no.monopixel.slimcolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Reproduction manager for colony wide reproduction (100% family friendly code).
 */
public interface IReproductionManager
{
    /**
     * On colony tick operation.
     * @param colony the colony ticking.
     */
    void onColonyTick(@NotNull final IColony colony);
}
