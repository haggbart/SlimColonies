package no.monopixel.slimcolonies.api.entity.ai.statemachine.tickratestatemachine;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IStateEventType;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.transitions.IStateMachineEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event with a tickrate for a statemachine using a tickrate.
 */
public class TickingEvent<S extends IState> extends TickingTransition<S> implements IStateMachineEvent<S>
{
    /**
     * The type of this Event
     */
    private final IStateEventType eventType;

    /**
     * Creates a new TickingEvent
     *
     * @param eventType The type of the event
     * @param condition condition when the event applies
     * @param nextState state the event transitions into
     * @param tickRate  tickrate at which the event is checked
     */
    protected TickingEvent(
      @NotNull final IStateEventType eventType,
        @NotNull final IBooleanConditionSupplier condition,
        @NotNull final IStateSupplier<S> nextState,
      final int tickRate)
    {
        super(condition, nextState, tickRate);
        this.eventType = eventType;
    }

    /**
     * Get the type of this event
     */
    @Override
    public IStateEventType getEventType()
    {
        return eventType;
    }
}
