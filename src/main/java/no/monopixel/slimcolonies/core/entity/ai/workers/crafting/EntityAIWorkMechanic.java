package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingMechanic;
import no.monopixel.slimcolonies.core.colony.jobs.JobMechanic;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts everything else basically (redstone stuff etc)
 */
public class EntityAIWorkMechanic extends AbstractEntityAICrafting<JobMechanic, BuildingMechanic>
{
    /**
     * Initialize the mechanic and add all his tasks.
     *
     * @param mechanic the job he has.
     */
    public EntityAIWorkMechanic(@NotNull final JobMechanic mechanic)
    {
        super(mechanic);
    }

    @Override
    public Class<BuildingMechanic> getExpectedBuildingClass()
    {
        return BuildingMechanic.class;
    }
}
