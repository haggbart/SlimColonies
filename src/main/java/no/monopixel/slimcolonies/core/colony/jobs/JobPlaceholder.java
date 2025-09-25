package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractAISkeleton;
import org.jetbrains.annotations.Nullable;

/**
 * Class of the placeholder job. Used if a certain building doesn't have a job yet.
 */
public class JobPlaceholder extends AbstractJob<AbstractAISkeleton<JobPlaceholder>, JobPlaceholder>
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobPlaceholder(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public AbstractAISkeleton<JobPlaceholder> generateAI()
    {
        return null;
    }
}
