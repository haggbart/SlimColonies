package no.monopixel.slimcolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.entity.ai.workers.builder.EntityAIStructureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * The job of the builder.
 */
public class JobBuilder extends AbstractJobStructure<EntityAIStructureBuilder, JobBuilder>
{
    /**
     * Instantiates builder job.
     *
     * @param entity citizen.
     */
    public JobBuilder(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.BUILDER_ID;
    }

    @NotNull
    @Override
    public EntityAIStructureBuilder generateAI()
    {
        return new EntityAIStructureBuilder(this);
    }
}
