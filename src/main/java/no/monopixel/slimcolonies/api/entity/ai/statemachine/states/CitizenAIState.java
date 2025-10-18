package no.monopixel.slimcolonies.api.entity.ai.statemachine.states;

/**
 * AI States for citizen's state
 */
public enum CitizenAIState implements IState
{
    IDLE(),
    FLEE(),
    EATING(),
    SICK(),
    SLEEP,
    WORK,
    WORKING,
    INACTIVE();

    CitizenAIState()
    {

    }
}
