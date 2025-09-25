package no.monopixel.slimcolonies.core.colony.jobs;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IAssignsJob;
import no.monopixel.slimcolonies.api.colony.jobs.IJobWithExternalWorkStations;
import no.monopixel.slimcolonies.core.colony.buildings.modules.QuarryModule;
import no.monopixel.slimcolonies.core.entity.ai.workers.production.EntityAIQuarrier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FIRE_DAMAGE_PREDICATE;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FIRE_RES;

/**
 * Special quarrier job. Defines miner model and specialized job behaviour.
 */
public class JobQuarrier extends AbstractJobStructure<EntityAIQuarrier, JobQuarrier> implements IJobWithExternalWorkStations
{
    /**
     * Creates a new instance of the miner job.
     *
     * @param entity the entity to add the job to.
     */
    public JobQuarrier(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.MINER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIQuarrier generateAI()
    {
        return new EntityAIQuarrier(this);
    }


    /**
     * Finds the quarry our miner is assigned to
     *
     * @return quarry building or null
     */
    public IBuilding findQuarry()
    {
        for (final IBuilding building : getColony().getBuildingManager().getBuildings().values())
        {
            if (building.getBuildingType().getRegistryName().getPath().contains("quarry") && building.getFirstModuleOccurance(QuarryModule.class).hasAssignedCitizen(getCitizen()))
            {
                return building;
            }
        }

        return null;
    }

    @Override
    public boolean assignTo(final IAssignsJob module)
    {
        if (module == null || !module.getJobEntry().equals(getJobRegistryEntry()))
        {
            return false;
        }

        if (module instanceof QuarryModule)
        {
            return true;
        }

        return super.assignTo(module);
    }

    @Override
    public List<IBuilding> getWorkStations()
    {
        final IBuilding building = findQuarry();
        return building == null ? Collections.emptyList() : ImmutableList.of(building);
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource.typeHolder().is(FIRE_DAMAGE_PREDICATE))
        {
            return getColony().getResearchManager().getResearchEffects().getEffectStrength(FIRE_RES) > 0;
        }

        return super.ignoresDamage(damageSource);
    }
}
