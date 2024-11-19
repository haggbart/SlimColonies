package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony flag changed event.
 */
public final class ColonyFlagChangedEvent extends AbstractColonyEvent
{
    /**
     * Constructs a colony flag changed event.
     *
     * @param colony the colony related to the event.
     */
    public ColonyFlagChangedEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
