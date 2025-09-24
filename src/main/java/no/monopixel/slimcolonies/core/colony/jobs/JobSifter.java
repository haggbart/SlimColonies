package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.entity.ai.workers.crafting.EntityAIWorkSifter;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * The sifter job class.
 */
public class JobSifter extends AbstractJobCrafter<EntityAIWorkSifter, JobSifter>
{
    /**
     * Create a sifter job.
     *
     * @param entity the lumberjack.
     */
    public JobSifter(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.SMELTER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkSifter generateAI()
    {
        return new EntityAIWorkSifter(this);
    }

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        worker.queueSound(SoundEvents.NETHER_BRICKS_HIT, blockPos, 1, 9);
    }
}
