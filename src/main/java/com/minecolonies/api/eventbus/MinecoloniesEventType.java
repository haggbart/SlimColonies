package com.minecolonies.api.eventbus;

import com.google.gson.reflect.TypeToken;

/**
 * Simple implementation for an event type.
 *
 * @param <T> the generic type of the event class.
 */
public final class MinecoloniesEventType<T extends IModEvent> implements IModEventType<T>
{
    /**
     * The class type of the event.
     */
    private final Class<T> eventClass;

    MinecoloniesEventType(final Class<T> eventClass)
    {
        this.eventClass = eventClass;
    }

    @Override
    public TypeToken<T> getIdentifier()
    {
        return TypeToken.get(this.eventClass);
    }
}
