package no.monopixel.slimcolonies.core.colony.buildings.moduleviews;

import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Student module view.
 */
public class StudentBuildingModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return true;
    }
}
