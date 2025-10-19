package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBaker;
import no.monopixel.slimcolonies.core.colony.jobs.JobBaker;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEMS_BAKED_DETAIL;

/**
 * Baker AI class.
 */
public class EntityAIWorkBaker extends AbstractEntityAIRequestSmelter<JobBaker, BuildingBaker>
{
    /**
     * Baking icon
     */
    private final static VisibleCitizenStatus BAKING =
        new VisibleCitizenStatus(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/icons/work/baker.png"), "no.monopixel.slimcolonies.gui.visiblestatus.baker");

    /**
     * Constructor for the Baker. Defines the tasks the bakery executes.
     *
     * @param job a bakery job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingBaker> getExpectedBuildingClass()
    {
        return BuildingBaker.class;
    }

    /**
     * Returns the bakery's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }

    @Override
    protected IAIState craft()
    {
        worker.getCitizenData().setVisibleStatus(BAKING);
        return super.craft();
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return true;
    }

    /**
     * Returns the name of the smelting stat that is used in the building's statistics.
     *
     * @return the name of the smelting stat.
     */
    protected String getSmeltingStatName()
    {
        return ITEMS_BAKED_DETAIL;
    }
}
