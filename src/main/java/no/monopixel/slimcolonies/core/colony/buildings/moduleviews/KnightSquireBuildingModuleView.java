package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Student module view.
 */
public class KnightSquireBuildingModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return jobEntry == ModJobs.knight.get();
    }
}
