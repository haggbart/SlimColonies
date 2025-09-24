package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.entity.ai.workers.production.herders.EntityAIWorkChickenHerder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;

/**
 * The Chicken Herder job
 */
public class JobChickenHerder extends AbstractJob<EntityAIWorkChickenHerder, JobChickenHerder>
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobChickenHerder(final ICitizenData entity)
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
    public EntityAIWorkChickenHerder generateAI()
    {
        return new EntityAIWorkChickenHerder(this);
    }

    @Override
    public boolean pickupSuccess(@NotNull final ItemStack pickedUpStack)
    {
        if (pickedUpStack.getItem() == Items.FEATHER || pickedUpStack.getItem() == Items.EGG)
        {
            return getCitizen().getRandom()
              .nextInt((getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModule(BuildingModules.CHICKENHERDER_WORK).getPrimarySkill()))) > 1;
        }
        return true;
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
        return ModModelTypes.CHICKEN_FARMER_ID;
    }

    @Override
    public boolean onStackPickUp(@NotNull final ItemStack pickedUpStack)
    {
        if (getCitizen().getWorkBuilding() != null && getCitizen().getEntity().isPresent() && getCitizen().getWorkBuilding()
          .isInBuilding(getCitizen().getEntity().get().blockPosition()))
        {
            StatsUtil.trackStatByName(getCitizen().getWorkBuilding(), ITEM_OBTAINED, pickedUpStack.getHoverName(), pickedUpStack.getCount());
        }

        return super.onStackPickUp(pickedUpStack);
    }
}
