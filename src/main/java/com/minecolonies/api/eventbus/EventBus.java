package com.minecolonies.api.eventbus;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for the mod event bus.
 */
public interface EventBus
{
    /**
     * Subscribe to the given event type, providing a handler function.
     *
     * @param eventType the event type to subscribe to.
     * @param handler   the handler function handling the event logic.
     * @param <T>       the generic type of the event class.
     */
    <T extends IModEvent> void subscribe(final @NotNull IModEventType<T> eventType, final @NotNull EventHandler<T> handler);

    /**
     * Posts a new event on the event bus for the given type.
     *
     * @param eventType the event type to subscribe to.
     * @param event     the event to send.
     * @param <T>       the generic type of the event class.
     */
    <T extends IModEvent> void post(final @NotNull IModEventType<T> eventType, final @NotNull T event);

    /**
     * The event handler lambda definition.
     *
     * @param <T> the generic type of the event class.
     */
    @FunctionalInterface
    interface EventHandler<T extends IModEvent>
    {
        void apply(final @NotNull T event);
    }
}
