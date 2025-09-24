package no.monopixel.slimcolonies.api.eventbus.events.colony;

import no.monopixel.slimcolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony name changed event.
 */
public final class ColonyNameChangedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a colony name changed event.
     *
     * @param colony the colony related to the event.
     */
    public ColonyNameChangedModEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
