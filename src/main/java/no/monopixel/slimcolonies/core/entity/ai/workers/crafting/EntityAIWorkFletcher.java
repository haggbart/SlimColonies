package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingFletcher;
import no.monopixel.slimcolonies.core.colony.jobs.JobFletcher;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkFletcher extends AbstractEntityAICrafting<JobFletcher, BuildingFletcher>
{
    /**
     * Initialize the fletcher and add all his tasks.
     *
     * @param fletcher the job he has.
     */
    public EntityAIWorkFletcher(@NotNull final JobFletcher fletcher)
    {
        super(fletcher);
    }

    @Override
    public Class<BuildingFletcher> getExpectedBuildingClass()
    {
        return BuildingFletcher.class;
    }
}
