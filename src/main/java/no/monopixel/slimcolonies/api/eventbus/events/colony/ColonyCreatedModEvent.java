package no.monopixel.slimcolonies.api.eventbus.events.colony;

import no.monopixel.slimcolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony created event.
 */
public final class ColonyCreatedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a colony created event.
     *
     * @param colony The colony related to the event.
     */
    public ColonyCreatedModEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
