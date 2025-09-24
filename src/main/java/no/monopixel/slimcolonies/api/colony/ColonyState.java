package no.monopixel.slimcolonies.api.colony;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;

/**
 * THe states a colony can be in.
 */
public enum ColonyState implements IAIState
{
    /**
     * Colony is not ticking
     */
    INACTIVE,

    /**
     * Colony is active
     */
    ACTIVE,

    /**
     * Colony chunks are not loaded
     */
    UNLOADED;

    @Override
    public boolean isOkayToEat()
    {
        return false;
    }
}
