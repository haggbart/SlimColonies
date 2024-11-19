package com.minecolonies.api.eventbus.events;

import com.minecolonies.api.eventbus.IModEvent;

import java.util.UUID;

/**
 * Abstract implementation for this mod bus events.
 */
public class AbstractEvent implements IModEvent
{
    /**
     * The unique id for this event.
     */
    private final UUID eventId;

    /**
     * Default constructor.
     */
    protected AbstractEvent()
    {
        this.eventId = UUID.randomUUID();
    }

    @Override
    public UUID getEventId()
    {
        return eventId;
    }
}
