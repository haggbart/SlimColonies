package no.monopixel.slimcolonies.api.eventbus.events.colony;

import no.monopixel.slimcolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony deleted event.
 */
public final class ColonyDeletedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a colony deleted event.
     *
     * @param colony The colony related to the event.
     */
    public ColonyDeletedModEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
