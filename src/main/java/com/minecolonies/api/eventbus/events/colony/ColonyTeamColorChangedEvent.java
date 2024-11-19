package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony team changed event.
 */
public final class ColonyTeamColorChangedEvent extends AbstractColonyEvent
{
    /**
     * Constructs a colony team changed event.
     *
     * @param colony the colony related to the event.
     */
    public ColonyTeamColorChangedEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
