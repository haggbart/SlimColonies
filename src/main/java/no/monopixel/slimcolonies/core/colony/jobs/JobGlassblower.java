package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.entity.ai.workers.crafting.EntityAIWorkGlassblower;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Glassblower job.
 */
public class JobGlassblower extends AbstractJobCrafter<EntityAIWorkGlassblower, JobGlassblower>
{
    /**
     * Instantiates the job for the Glassblower.
     *
     * @param entity the citizen who becomes a Glassblower.
     */
    public JobGlassblower(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.GLASSBLOWER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkGlassblower generateAI()
    {
        return new EntityAIWorkGlassblower(this);
    }

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        if (worker.getRandom().nextInt(10) < 1)
        {
            worker.queueSound(SoundEvents.REDSTONE_TORCH_BURNOUT, blockPos, 10, 0);
        }
        else
        {
            worker.queueSound(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, blockPos, 5, 0);
            worker.queueSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, blockPos, 5, 0);
        }
    }
}
