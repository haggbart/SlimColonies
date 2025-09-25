package no.monopixel.slimcolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.entity.ai.workers.education.EntityAIWorkTeacher;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the teacher.
 */
public class JobTeacher extends AbstractJob<EntityAIWorkTeacher, JobTeacher>
{
    /**
     * Public constructor of the teacher job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobTeacher(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.TEACHER_ID;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkTeacher generateAI()
    {
        return new EntityAIWorkTeacher(this);
    }
}
