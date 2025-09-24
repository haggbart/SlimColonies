package no.monopixel.slimcolonies.core.entity.ai.workers.guard;

import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingGuards;
import no.monopixel.slimcolonies.core.colony.jobs.JobRanger;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.PathJobWalkRandomEdge;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.ARCHER_USE_ARROWS;

/**
 * Ranger AI class, which deals with equipment and movement specifics
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger, AbstractBuildingGuards>
{
    public static final String RENDER_META_ARROW = "arrow";

    public EntityAIRanger(@NotNull final JobRanger job)
    {
        super(job);
        toolsNeeded.add(ModEquipmentTypes.bow.get());
        new RangerCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.ARROW))
        {
            renderMeta += RENDER_META_ARROW;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    protected void atBuildingActions()
    {
        super.atBuildingActions();

        if (worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0)
        {
            // Pickup arrows and request arrows
            InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(building,
              item -> item.getItem() instanceof ArrowItem,
              64,
              worker.getInventoryCitizen());

            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem) < 16)
            {
                checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.ARROW), 64, 16);
            }
        }
    }

    @Override
    public void guardMovement()
    {
        if (worker.getRandom().nextInt(3) < 1)
        {
            walkToSafePos(buildingGuards.getGuardPos(worker));
            return;
        }

        if ((BlockPosUtil.dist(buildingGuards.getGuardPos(worker), worker.blockPosition()) <= 10 || walkToSafePos(buildingGuards.getGuardPos(worker)))
            || Math.abs(buildingGuards.getGuardPos(worker).getY() - worker.blockPosition().getY()) > 3)
        {
            // Moves the ranger randomly to close edges, for better vision to mobs
            ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(new PathJobWalkRandomEdge(world, buildingGuards.getGuardPos(worker), 20, worker),
              null,
              1.0, true);
        }
    }
}
