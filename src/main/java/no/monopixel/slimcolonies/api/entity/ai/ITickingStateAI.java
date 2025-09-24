package no.monopixel.slimcolonies.api.entity.ai;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;

/**
 * Interface for ticking AI's
 */
public interface ITickingStateAI
{
    /**
     * Ticks the ai
     */
    public void tick();

    /**
     * Get the statemachine of the AI
     *
     * @return statemachine
     */
    ITickRateStateMachine<IAIState> getStateAI();

    /**
     * Called when the AI get removed
     */
    public void onRemoval();

    /**
     * Resets the AI as needed
     */
    public void resetAI();

    /**
     * Gets the current state
     *
     * @return
     */
    public IState getState();
}
