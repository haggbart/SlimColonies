package no.monopixel.slimcolonies.core.entity.ai.minimal;

import no.monopixel.slimcolonies.api.entity.ai.IStateAI;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.CitizenAIState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobGuard;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander implements IStateAI
{
    /**
     * The citizen that is wandering.
     */
    protected final EntityCitizen citizen;

    /**
     * Wandering speed.
     */
    protected final double speed;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAICitizenWander(final EntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.IDLE, () -> true, this::decide, 100));
    }

    private IState decide()
    {
        if (canUse())
        {
            EntityNavigationUtils.walkToRandomPos(citizen, 10, this.speed);
        }
        return CitizenAIState.IDLE;
    }

    public boolean canUse()
    {
        return citizen.getNavigation().isDone() && !citizen.isBaby()
            && !(citizen.getCitizenData().getJob() instanceof AbstractJobGuard);
    }
}
