package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingSawmill;
import no.monopixel.slimcolonies.core.colony.jobs.JobSawmill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkSawmill extends AbstractEntityAICrafting<JobSawmill, BuildingSawmill>
{
    /**
     * Crafting icon
     */
    private final static VisibleCitizenStatus CRAFTING =
        new VisibleCitizenStatus(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/icons/work/sawmill.png"), "no.monopixel.slimcolonies.gui.visiblestatus.sawmill");

    /**
     * Initialize the sawmill and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkSawmill(@NotNull final JobSawmill sawmill)
    {
        super(sawmill);
    }

    @Override
    public Class<BuildingSawmill> getExpectedBuildingClass()
    {
        return BuildingSawmill.class;
    }

    @Override
    protected IAIState craft()
    {
        worker.getCitizenData().setVisibleStatus(CRAFTING);
        return super.craft();
    }
}
