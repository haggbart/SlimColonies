package no.monopixel.slimcolonies.api.eventbus.events;

import no.monopixel.slimcolonies.api.colony.IColonyManager;
import org.jetbrains.annotations.NotNull;

/**
 * Colony manager unloaded event.
 */
public final class ColonyManagerUnloadedModEvent extends AbstractModEvent
{
    /**
     * The colony manager instance.
     */
    @NotNull
    private final IColonyManager colonyManager;

    /**
     * Event for colony manager loaded.
     */
    public ColonyManagerUnloadedModEvent(final @NotNull IColonyManager colonyManager)
    {
        this.colonyManager = colonyManager;
    }

    /**
     * Get the colony manager instance.
     *
     * @return the colony manager.
     */
    @NotNull
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }
}
