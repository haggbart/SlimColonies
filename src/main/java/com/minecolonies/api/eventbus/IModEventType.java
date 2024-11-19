package com.minecolonies.api.eventbus;

import com.google.gson.reflect.TypeToken;

/**
 * Interface for event types, must define an identifier.
 *
 * @param <T> the generic type of the event class.
 */
public interface IModEventType<T extends IModEvent>
{
    TypeToken<T> getIdentifier();
}
