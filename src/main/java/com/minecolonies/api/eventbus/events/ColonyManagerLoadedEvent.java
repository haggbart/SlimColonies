package com.minecolonies.api.eventbus.events;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.eventbus.IModEvent;
import com.minecolonies.api.eventbus.IModEventType;
import org.jetbrains.annotations.NotNull;

/**
 * Colony manager loaded event.
 */
public final class ColonyManagerLoadedEvent extends AbstractEvent
{
    /**
     * The colony manager instance.
     */
    @NotNull
    private final IColonyManager colonyManager;

    /**
     * Event for colony manager loaded.
     */
    public ColonyManagerLoadedEvent(final @NotNull IColonyManager colonyManager)
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
