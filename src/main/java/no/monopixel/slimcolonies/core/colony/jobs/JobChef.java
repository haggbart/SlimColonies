package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.core.entity.ai.workers.crafting.EntityAIWorkChef;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Chef job.
 */
public class JobChef extends AbstractJobCrafter<EntityAIWorkChef, JobChef>
{
    /**
     * Instantiates the job for the Chef.
     *
     * @param entity the citizen who becomes a Chef.
     */
    public JobChef(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.COOK_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkChef generateAI()
    {
        return new EntityAIWorkChef(this);
    }

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        worker.queueSound(SoundEvents.FIRE_AMBIENT, blockPos, 5, 0);
        if (worker.getRandom().nextBoolean())
        {
            worker.queueSound(SoundEvents.COPPER_HIT, blockPos, 5, 0);
        }
    }
}
