package no.monopixel.slimcolonies.core.entity.citizen.citizenhandlers;

import no.monopixel.slimcolonies.api.advancements.AdvancementTriggers;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.jobs.IJob;
import no.monopixel.slimcolonies.api.entity.ai.ITickingStateAI;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.citizenhandlers.ICitizenJobHandler;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobGuard;
import no.monopixel.slimcolonies.core.util.AdvancementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen.DATA_MODEL;

/**
 * Handles the citizen job methods.
 */
public class CitizenJobHandler implements ICitizenJobHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * The job's work AI
     */
    private ITickingStateAI workAI = null;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenJobHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Set Model depending on job.
     *
     * @param job the new job.
     */
    @Override
    public void setModelDependingOnJob(@Nullable final IJob<?> job)
    {
        if (citizen.isBaby())
        {
            citizen.setModelId(ModModelTypes.CHILD_ID);
            citizen.getEntityData().set(DATA_MODEL, citizen.getModelType().toString());
            citizen.setRenderMetadata("");
            return;
        }

        if (job == null)
        {
            if (citizen.getCitizenColonyHandler().getHomeBuilding() != null)
            {
                switch (citizen.getCitizenColonyHandler().getHomeBuilding().getBuildingLevelEquivalent())
                {
                    case 3:
                        citizen.setModelId(ModModelTypes.CITIZEN_ID);
                        break;
                    case 4:
                        citizen.setModelId(ModModelTypes.NOBLE_ID);
                        break;
                    case 5:
                        citizen.setModelId(ModModelTypes.ARISTOCRAT_ID);
                        break;
                    default:
                        citizen.setModelId(ModModelTypes.SETTLER_ID);
                        break;
                }
            }
            else
            {
                citizen.setModelId(ModModelTypes.SETTLER_ID);
            }
        }
        else
        {
            citizen.setModelId(job.getModel());
        }

        citizen.getEntityData().set(DATA_MODEL, citizen.getModelType().toString());
        citizen.setRenderMetadata("");
    }

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    @Override
    public void onJobChanged(@Nullable final IJob<?> job)
    {
        //  Model
        setModelDependingOnJob(job);

        if (job != null)
        {
            job.createAI();

            // Calculate the number of guards for some advancements
            if (job instanceof AbstractJobGuard)
            {
                IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
                int guards = ((int) colony.getCitizenManager().getCitizens()
                  .stream()
                  .filter(citizen -> citizen.getJob() instanceof AbstractJobGuard)
                  .count());
                AdvancementUtils.TriggerAdvancementPlayersForColony(citizen.getCitizenColonyHandler().getColonyOrRegister(),
                  player -> AdvancementTriggers.ARMY_POPULATION.trigger(player, guards));
            }

            job.initEntityValues(citizen);
        }
    }

    /**
     * Get the job of the citizen.
     *
     * @param type of the type.
     * @param <J>  wildcard.
     * @return the job.
     */
    @Override
    @Nullable
    public <J extends IJob<?>> J getColonyJob(@NotNull final Class<J> type)
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob(type);
    }

    /**
     * Gets the job of the entity.
     *
     * @return the job or els enull.
     */
    @Override
    @Nullable
    public IJob<?> getColonyJob()
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob();
    }

    @Override
    public boolean shouldRunAvoidance()
    {
        return getColonyJob() == null || getColonyJob().allowsAvoidance();
    }

    @Override
    public void setWorkAI(final ITickingStateAI workAI)
    {
        this.workAI = workAI;
    }

    @Override
    public ITickingStateAI getWorkAI()
    {
        return workAI;
    }
}
