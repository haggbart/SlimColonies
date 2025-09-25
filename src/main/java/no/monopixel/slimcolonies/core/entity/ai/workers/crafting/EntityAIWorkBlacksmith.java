package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBlacksmith;
import no.monopixel.slimcolonies.core.colony.jobs.JobBlacksmith;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts tools and armour.
 */
public class EntityAIWorkBlacksmith extends AbstractEntityAICrafting<JobBlacksmith, BuildingBlacksmith>
{
    /**
     * Initialize the blacksmith and add all his tasks.
     *
     * @param blacksmith the job he has.
     */
    public EntityAIWorkBlacksmith(@NotNull final JobBlacksmith blacksmith)
    {
        super(blacksmith);
    }

    @Override
    public Class<BuildingBlacksmith> getExpectedBuildingClass()
    {
        return BuildingBlacksmith.class;
    }

}
