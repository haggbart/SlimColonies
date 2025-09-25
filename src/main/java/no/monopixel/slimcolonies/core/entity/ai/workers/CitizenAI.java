package no.monopixel.slimcolonies.core.entity.ai.workers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.entity.ai.IStateAI;
import no.monopixel.slimcolonies.api.entity.ai.ITickingStateAI;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AIEventTarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.CitizenAIState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IState;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.util.CompatibilityUtils;
import no.monopixel.slimcolonies.api.util.MathUtils;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.api.util.constant.CitizenConstants;
import no.monopixel.slimcolonies.core.colony.interactionhandling.StandardInteraction;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobGuard;
import no.monopixel.slimcolonies.core.colony.jobs.JobPupil;
import no.monopixel.slimcolonies.core.entity.ai.minimal.*;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen.ENTITY_AI_TICKRATE;
import static no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus.*;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING;
import static no.monopixel.slimcolonies.core.entity.ai.minimal.EntityAIEatTask.RESTAURANT_LIMIT;
import static no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers.CitizenInjuryHandler.SEEK_DOCTOR_HEALTH;

/**
 * High level AI for citizens, which switches between all the different AI states like sleeping,working,fleeing etc
 */
public class CitizenAI implements IStateAI
{
    /**
     * Citizen this AI belongs to
     */
    private final EntityCitizen citizen;

    /**
     * The last citizen AI state
     */
    private IState lastState = CitizenAIState.IDLE;

    /**
     * List of small AI's added
     */
    private List<IStateAI> minimalAI = new ArrayList<>();

    public CitizenAI(final EntityCitizen citizen)
    {
        this.citizen = citizen;

        citizen.getCitizenAI().addTransition(new AIEventTarget<IState>(AIBlockingEventType.EVENT, () -> true, this::decideAiTask, 10));
        registerWorkAI();

        minimalAI.add(new EntityAICitizenAvoidEntity(citizen, Monster.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        minimalAI.add(new EntityAIEatTask(citizen));
        minimalAI.add(new EntityAICitizenWander(citizen, DEFAULT_SPEED));
        minimalAI.add(new EntityAIInjuredTask(citizen));
        minimalAI.add(new EntityAISleep(citizen));
        minimalAI.add(new EntityAIMournCitizen(citizen, DEFAULT_SPEED));
    }

    /**
     * Registers callbacks for the work/job AI
     */
    private void registerWorkAI()
    {
        citizen.getCitizenAI().addTransition(new AITarget<>(CitizenAIState.WORK, () -> true, () ->
        {
            final ITickingStateAI ai = citizen.getCitizenJobHandler().getColonyJob().getWorkerAI();
            if (ai != null)
            {
                ai.resetAI();
            }
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
            return CitizenAIState.WORKING;
        }, 10));

        citizen.getCitizenAI().addTransition(new AITarget<>(CitizenAIState.WORKING, () -> true, () ->
        {
            if (citizen.getCitizenJobHandler().getColonyJob() != null)
            {
                final ITickingStateAI ai = citizen.getCitizenJobHandler().getColonyJob().getWorkerAI();
                if (ai != null)
                {
                    citizen.getCitizenJobHandler().getColonyJob().getWorkerAI().tick();
                }
            }

            return CitizenAIState.WORKING;
        }, ENTITY_AI_TICKRATE));
    }

    /**
     * Checks on the AI state the citizen should be in, and transitions as necessary
     *
     * @return
     */
    private IState decideAiTask()
    {
        IState next = calculateNextState();
        if (next == null || next == lastState)
        {
            return null;
        }

        citizen.getCitizenData().setVisibleStatus(null);
        lastState = next;
        return lastState;
    }

    /**
     * Determines the AI state the citizen should be doing, sleeping,raiding etc at different priorities
     *
     * @return
     */
    private IState calculateNextState()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard guardJob)
        {
            if (shouldEat())
            {
                return CitizenAIState.EATING;
            }

            // Sick
            if (citizen.getCitizenData().getCitizenInjuryHandler().isHurt() && guardJob.canAIBeInterrupted())
            {
                citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
                return CitizenAIState.SICK;
            }

            return CitizenAIState.WORK;
        }

        // Sick at hospital
        if (citizen.getCitizenData().getCitizenInjuryHandler().isHurt() && citizen.getCitizenData().getCitizenInjuryHandler().sleepsAtHospital())
        {
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
            return CitizenAIState.SICK;
        }


        // Sleeping
        if (!WorldUtil.isPastTime(CompatibilityUtils.getWorldFromCitizen(citizen), NIGHT - 2000))
        {
            if (lastState == CitizenAIState.SLEEP)
            {
                citizen.setVisibleStatusIfNone(SLEEP);
                citizen.getCitizenAI().setCurrentDelay(20 * 15);
                return CitizenAIState.SLEEP;
            }

            if (citizen.getCitizenSleepHandler().shouldGoSleep())
            {
                return CitizenAIState.SLEEP;
            }
        }
        else
        {
            if (citizen.getCitizenSleepHandler().isAsleep())
            {
                if (citizen.getCitizenData().getCitizenInjuryHandler().isHurt())
                {
                    final BlockPos bedPos = citizen.getCitizenSleepHandler().getBedLocation();
                    if (bedPos == null || bedPos.distSqr(citizen.blockPosition()) > 5)
                    {
                        citizen.getCitizenSleepHandler().onWakeUp();
                    }
                }
                else
                {
                    citizen.getCitizenSleepHandler().onWakeUp();
                }
            }
        }

        // Sick
        if (citizen.getCitizenData().getCitizenInjuryHandler().isHurt() || citizen.getCitizenData().getCitizenInjuryHandler().isHurt())
        {
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
            return CitizenAIState.SICK;
        }

        // Eating
        if (shouldEat())
        {
            return CitizenAIState.EATING;
        }

        // Mourning
        if (citizen.getCitizenData().getCitizenMournHandler().isMourning())
        {
            if (lastState != CitizenAIState.MOURN)
            {
                citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING,
                    citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens().iterator().next()),
                    Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
                    ChatPriority.IMPORTANT));

                citizen.setVisibleStatusIfNone(MOURNING);
            }
            return CitizenAIState.MOURN;
        }


        // Work
        if (citizen.isBaby() && citizen.getCitizenJobHandler().getColonyJob() instanceof JobPupil && citizen.level.getDayTime() % 24000 > NOON)
        {
            citizen.setVisibleStatusIfNone(HOUSE);
            return CitizenAIState.IDLE;
        }

        if (citizen.getCitizenJobHandler().getColonyJob() != null
            && citizen.getCitizenJobHandler().getColonyJob().getWorkerAI() instanceof AbstractEntityAIBasic<?, ?> abstractEntityAIBasic && !abstractEntityAIBasic.canGoIdle()
            && (citizen.getCitizenData().getLeisureTime() <= 0
            || !citizen.getCitizenData().getJob().canAIBeInterrupted()))
        {
            citizen.setVisibleStatusIfNone(WORKING);
            return CitizenAIState.WORK;
        }

        citizen.setVisibleStatusIfNone(HOUSE);
        return CitizenAIState.IDLE;
    }

    /**
     * Checks if the citizen should be eating
     *
     * @return
     */
    public boolean shouldEat()
    {
        if (citizen.getCitizenData().justAte())
        {
            return false;
        }

        if (citizen.getCitizenData().getJob() != null && (!citizen.getCitizenData().getJob().canAIBeInterrupted()))
        {
            return false;
        }

        if (lastState == CitizenAIState.EATING)
        {
            return true;
        }

        if (citizen.getCitizenData().getJob() != null && (citizen.getCitizenData().getJob().getJobRegistryEntry() == ModJobs.cook.get()) && MathUtils.RANDOM.nextInt(20) > 0)
        {
            return false;
        }

        if (citizen.getCitizenData().getCitizenInjuryHandler().isHurt() && citizen.getCitizenSleepHandler().isAsleep())
        {
            return false;
        }

        return citizen.getCitizenData().getSaturation() <= CitizenConstants.AVERAGE_SATURATION &&
            (citizen.getCitizenData().getSaturation() <= RESTAURANT_LIMIT ||
                (citizen.getCitizenData().getSaturation() < LOW_SATURATION && citizen.getHealth() < SEEK_DOCTOR_HEALTH));
    }
}
