package no.monopixel.slimcolonies.api.entity.ai.statemachine.transitions;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IStateEventType;

/**
 * Event transition type for Statemachines
 */
public interface IStateMachineEvent<S extends IState> extends IStateMachineTransition<S>
{
    /**
     * Get the event type of the transition
     *
     * @return event type
     */
    IStateEventType getEventType();
}
