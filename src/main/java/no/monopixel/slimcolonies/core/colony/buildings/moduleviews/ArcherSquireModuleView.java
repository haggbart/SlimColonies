package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import no.monopixel.slimcolonies.api.colony.jobs.ModJobs;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Archery module view.
 */
public class ArcherSquireModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return jobEntry == ModJobs.archer.get();
    }
}
