package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.eventbus.events.AbstractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Any colony related event, provides the target colony the event occurred in.
 */
public abstract class AbstractColonyEvent extends AbstractEvent
{
    /**
     * The colony this event was called in.
     */
    @NotNull
    private final IColony colony;

    /**
     * Constructs a colony-based event.
     *
     * @param colony The colony related to the event.
     */
    protected AbstractColonyEvent(@NotNull final IColony colony)
    {
        this.colony = colony;
    }

    /**
     * Gets the colony related to the event.
     */
    @NotNull
    public IColony getColony()
    {
        return colony;
    }
}
