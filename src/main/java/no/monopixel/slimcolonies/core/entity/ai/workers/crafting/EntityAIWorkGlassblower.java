package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingGlassblower;
import no.monopixel.slimcolonies.core.colony.jobs.JobGlassblower;

import org.jetbrains.annotations.NotNull;

/**
 * Crafts glass relates things, crafts and smelts.
 */
public class EntityAIWorkGlassblower extends AbstractEntityAIRequestSmelter<JobGlassblower, BuildingGlassblower>
{
    /**
     * Initialize the glass blower AI.
     *
     * @param glassBlower the job he has.
     */
    public EntityAIWorkGlassblower(@NotNull final JobGlassblower glassBlower)
    {
        super(glassBlower);
    }

    @Override
    public Class<BuildingGlassblower> getExpectedBuildingClass()
    {
        return BuildingGlassblower.class;
    }
}
