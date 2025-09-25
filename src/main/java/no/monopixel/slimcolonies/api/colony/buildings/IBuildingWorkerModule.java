package no.monopixel.slimcolonies.api.colony.buildings;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.jobs.IJob;
import no.monopixel.slimcolonies.api.colony.jobs.registry.JobEntry;
import no.monopixel.slimcolonies.api.entity.citizen.Skill;
import org.jetbrains.annotations.NotNull;

public interface IBuildingWorkerModule
{
    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    IJob<?> createJob(ICitizenData citizen);


    /**
     * Primary skill getter.
     *
     * @return the primary skill.
     */
    @NotNull
    Skill getPrimarySkill();

    /**
     * Secondary skill getter.
     *
     * @return the secondary skill.
     */
    @NotNull
    Skill getSecondarySkill();

    /**
     * Getter for the job entry.
     * @return the entry.
     */
    JobEntry getJobEntry();
}
