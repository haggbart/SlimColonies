package no.monopixel.slimcolonies.core.entity.ai.workers;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.workorders.IBuilderWorkOrder;
import no.monopixel.slimcolonies.api.colony.workorders.IWorkOrder;
import no.monopixel.slimcolonies.api.colony.workorders.WorkOrderType;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.eventbus.events.colony.buildings.BuildingConstructionModEvent;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.util.*;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import no.monopixel.slimcolonies.core.colony.buildings.utils.BuildingBuilderResource;
import no.monopixel.slimcolonies.core.colony.eventhooks.buildingEvents.BuildingBuiltEvent;
import no.monopixel.slimcolonies.core.colony.eventhooks.buildingEvents.BuildingDeconstructedEvent;
import no.monopixel.slimcolonies.core.colony.eventhooks.buildingEvents.BuildingRepairedEvent;
import no.monopixel.slimcolonies.core.colony.eventhooks.buildingEvents.BuildingUpgradedEvent;
import no.monopixel.slimcolonies.core.colony.jobs.AbstractJobStructure;
import no.monopixel.slimcolonies.core.colony.workorders.WorkOrderBuilding;
import no.monopixel.slimcolonies.core.colony.workorders.WorkOrderMiner;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.BuildingProgressStage;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.WorkerLoadOnlyStructureHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_START;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
@SuppressWarnings("removal")
public abstract class AbstractEntityAIStructureWithWorkOrder<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder>
    extends AbstractEntityAIStructure<J, B>
{
    /**
     * Possible request stages
     */
    protected enum RequestStage
    {
        SOLID,
        WEAK_SOLID,
        DECO,
        ENTITIES
    }

    /**
     * The current request state (0 is solid
     */
    protected RequestStage requestState = RequestStage.SOLID;

    /**
     * Request progress pos.
     */
    protected BlockPos requestProgress = null;

    /**
     * Variable telling us if we already recalculated the list.
     * We don't want to persist this anywhere on purpose.
     */
    private boolean recalculated = false;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public AbstractEntityAIStructureWithWorkOrder(@NotNull final J job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public void storeProgressPos(final BlockPos blockPos, final BuildingProgressStage stage)
    {
        building.setProgressPos(blockPos, stage);
        worker.getCitizenData().setStatusPosition(blockPos.equals(NULL_POS) ? null : structurePlacer.getB().getProgressPosInWorld(blockPos));
    }

    @Override
    public Tuple<BlockPos, BuildingProgressStage> getProgressPos()
    {
        return building.getProgress();
    }

    /**
     * Takes the existing workorder, loads the structure and tests the worker order if it is valid.
     */
    @Override
    public IAIState loadRequirements()
    {
        if (loadingBlueprint)
        {
            return getState();
        }

        if (job.getWorkOrder() == null || job.getWorkOrder().getBlueprint() == null || structurePlacer == null)
        {
            loadStructure();
            final IBuilderWorkOrder wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                    String.format("Worker (%d:%d) ERROR - Starting and missing work order(%d)",
                        worker.getCitizenColonyHandler().getColonyOrRegister().getID(),
                        worker.getCitizenData().getId(), job.getWorkOrderId()), new Exception());
                job.setWorkOrder(null);
                return IDLE;
            }

            if (wo instanceof WorkOrderBuilding)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                        String.format("Worker (%d:%d) ERROR - Starting and missing building(%s)",
                            worker.getCitizenColonyHandler().getColonyOrRegister().getID(), worker.getCitizenData().getId(), wo.getLocation()), new Exception());
                    return IDLE;
                }

                MessageUtils.forCitizen(worker, COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_START, job.getWorkOrder().getDisplayName())
                    .sendTo(worker.getCitizenColonyHandler().getColonyOrRegister().getMessagePlayerEntities());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
            else if (!(wo instanceof WorkOrderMiner))
            {
                MessageUtils.forCitizen(worker, COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_START, job.getWorkOrder().getDisplayName())
                    .sendTo(worker.getCitizenColonyHandler().getColonyOrRegister().getMessagePlayerEntities());
                ;
            }
            return getState();
        }

        if (job.getWorkOrder().isRequested())
        {
            return afterStructureLoading();
        }

        //We need to deal with materials
        requestMaterialsState();
        return getState();
    }

    /**
     * Load the structure into the AI.
     */
    private void loadStructure()
    {
        final IBuilderWorkOrder workOrder = job.getWorkOrder();

        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getLocation();
        if (workOrder instanceof WorkOrderBuilding && worker.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getCitizenColonyHandler().getColonyOrRegister().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        final boolean removal = workOrder.getWorkOrderType() == WorkOrderType.REMOVE;

        loadStructure(workOrder, pos, removal);
        workOrder.setCleared(false);
        workOrder.setRequested(removal);
    }

    /**
     * State for material requesting.
     */
    private void requestMaterialsState()
    {
        if (Constants.BUILDER_INF_RESOURECES || job.getWorkOrder().isRequested() || job.getWorkOrder().getWorkOrderType() == WorkOrderType.REMOVE)
        {
            recalculated = true;
            return;
        }

        final AbstractBuildingStructureBuilder buildingWorker = building;
        if (requestMaterials())
        {
            job.getWorkOrder().setRequested(true);
        }
        int newQuantity = buildingWorker.getNeededResources().values().stream().mapToInt(ItemStorage::getAmount).sum();
        if (job.getWorkOrder().getAmountOfResources() == 0 || newQuantity > job.getWorkOrder().getAmountOfResources())
        {
            job.getWorkOrder().setAmountOfResources(newQuantity);
        }
    }

    @Override
    protected boolean checkIfNeedsItem()
    {
        if (job.hasWorkOrder() && building.getNeededResources().isEmpty() && !building.hasCitizenCompletedRequests(worker.getCitizenData()) && !recalculated && (
            structurePlacer == null || !structurePlacer.getB().hasBluePrint() || !job.getWorkOrder().isRequested()))
        {
            return false;
        }
        return super.checkIfNeedsItem();
    }

    @Override
    public boolean requestMaterials()
    {
        StructurePhasePlacementResult result;
        final WorkerLoadOnlyStructureHandler structure = new WorkerLoadOnlyStructureHandler(world,
            structurePlacer.getB().getWorldPos(),
            structurePlacer.getB().getBluePrint(),
            new PlacementSettings(),
            true,
            this);

        if (job.getWorkOrder().getIteratorType().isEmpty())
        {
            final String mode = BuilderModeSetting.getActualValue(building);
            job.getWorkOrder().setIteratorType(mode);
        }

        final StructurePlacer placer = new StructurePlacer(structure, job.getWorkOrder().getIteratorType());

        if (requestProgress == null)
        {
            final AbstractBuildingStructureBuilder buildingWorker = building;
            buildingWorker.resetNeededResources();
            requestProgress = NULL_POS;
            requestState = RequestStage.SOLID;
        }

        final RequestStage currState = requestState;
        switch (currState)
        {
            case SOLID:
                result = placer.executeStructureStep(world,
                    null,
                    requestProgress,
                    StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                    () -> placer.getIterator()
                        .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !BlockUtils.canBlockFloatInAir(info.getBlockInfo().getState())
                            || isDecoItem(info.getBlockInfo()
                            .getState()
                            .getBlock()))),
                    false);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.WEAK_SOLID;
                }
                return false;

            case WEAK_SOLID:
                result = placer.executeStructureStep(world,
                    null,
                    requestProgress,
                    StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                    () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !BlockUtils.isWeakSolidBlock(info.getBlockInfo().getState()))),
                    false);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.DECO;
                }
                return false;

            case DECO:
                result = placer.executeStructureStep(world,
                    null,
                    requestProgress,
                    StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                    () -> placer.getIterator()
                        .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> BlockUtils.isAnySolid(info.getBlockInfo().getState()) && !isDecoItem(info.getBlockInfo()
                            .getState()
                            .getBlock()))),
                    false);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.ENTITIES;
                }
                return false;
            case ENTITIES:
                result = placer.executeStructureStep(world, null, requestProgress, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                    () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getEntities().length == 0)), true);
                requestProgress = result.getIteratorPos();

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.SOLID;
                    requestProgress = null;
                    recalculated = true;
                    return true;
                }
                return false;
            default:
                recalculated = true;
                return true;
        }
    }

    @Override
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        if (building.getNeededResources().get(stack.getDescriptionId() + "-" + hashCode) == null)
        {
            building.addNeededResource(stack, 1);
        }
    }

    @Override
    public int getTotalRequiredAmount(final ItemStack deliveredItemStack)
    {
        if (ItemStackUtils.isEmpty(deliveredItemStack))
        {
            return 0;
        }
        final int hashCode = deliveredItemStack.hasTag() ? deliveredItemStack.getTag().hashCode() : 0;
        final BuildingBuilderResource resource = building.getNeededResources().get(deliveredItemStack.getDescriptionId() + "-" + hashCode);
        if (resource != null)
        {
            return resource.getAmount();
        }

        return super.getTotalRequiredAmount(deliveredItemStack);
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        if (job.getWorkOrder().getBlueprint() == null && job.hasWorkOrder())
        {
            //fix for bad structures
            job.complete();
        }

        if (job.getWorkOrder().getBlueprint() == null)
        {
            return;
        }

        final IWorkOrder wo = job.getWorkOrder();

        if (wo == null)
        {
            Log.getLogger().error(String.format("Worker (%d:%d) ERROR - Finished, but missing work order(%d)",
                worker.getCitizenColonyHandler().getColonyOrRegister().getID(),
                worker.getCitizenData().getId(),
                job.getWorkOrderId()));
        }
        else
        {
            // TODO: Preferably want to use the display name of the building (in order to respect custom name) however this will require an event rework so it stores text components rather than strings
            String workOrderName = wo.getTranslationKey();
            sendCompletionMessage(wo);
            final IColony colony = job.getColony();
            switch (wo.getWorkOrderType())
            {
                case BUILD:
                    StatsUtil.trackStat(worker.getCitizenData().getWorkBuilding(), BUILD_BUILT, 1);
                    colony.getEventDescriptionManager().addEventDescription(new BuildingBuiltEvent(wo.getLocation(), workOrderName));
                    worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(BUILD_BUILT, colony.getDay());
                    break;
                case UPGRADE:
                    StatsUtil.trackStat(worker.getCitizenData().getWorkBuilding(), BUILD_UPGRADED, 1);
                    colony.getEventDescriptionManager().addEventDescription(new BuildingUpgradedEvent(wo.getLocation(), workOrderName, wo.getTargetLevel()));
                    worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(BUILD_UPGRADED, colony.getDay());
                    break;
                case REPAIR:
                    StatsUtil.trackStat(worker.getCitizenData().getWorkBuilding(), BUILD_REPAIRED, 1);
                    colony.getEventDescriptionManager().addEventDescription(new BuildingRepairedEvent(wo.getLocation(), workOrderName, wo.getCurrentLevel()));
                    worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(BUILD_REPAIRED, colony.getDay());
                    break;
                case REMOVE:
                    StatsUtil.trackStat(worker.getCitizenData().getWorkBuilding(), BUILD_REMOVED, 1);
                    colony.getEventDescriptionManager().addEventDescription(new BuildingDeconstructedEvent(wo.getLocation(), workOrderName, wo.getCurrentLevel()));
                    worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(BUILD_REMOVED, colony.getDay());
                    break;
            }

            job.complete();

            if (wo instanceof WorkOrderBuilding workOrderBuilding)
            {
                final IBuilding building = colony.getBuildingManager().getBuilding(wo.getLocation());
                if (building == null)
                {
                    Log.getLogger()
                        .error("Builder ({}:{}) ERROR - Finished, but missing building({})",
                            worker.getCitizenColonyHandler().getColonyOrRegister().getID(),
                            worker.getCitizenData().getId(),
                            wo.getLocation());
                }
                else
                {
                    switch (wo.getWorkOrderType())
                    {
                        case BUILD:
                        case UPGRADE:
                        case REPAIR:
                            // Normally levels are done through the schematic data, but in case it is missing we do it manually here.
                            final BlockEntity te = worker.level.getBlockEntity(building.getID());
                            if (te instanceof AbstractTileEntityColonyBuilding && ((IBlueprintDataProviderBE) te).getSchematicName().isEmpty())
                            {
                                building.onUpgradeComplete(wo.getTargetLevel());
                                building.setBuildingLevel(wo.getTargetLevel());
                            }
                            break;
                        case REMOVE:
                            building.setDeconstructed();
                            break;
                    }
                    IMinecoloniesAPI.getInstance().getEventBus().post(new BuildingConstructionModEvent(building, workOrderBuilding));
                }
            }
        }
        building.resetNeededResources();
    }

    /**
     * Send a completion message to the colony if necessary.
     *
     * @param wo the completed workorder.
     */
    protected void sendCompletionMessage(final IWorkOrder wo)
    {
        //noop
    }

    @Override
    public void reduceNeededResources(final ItemStack stack)
    {
        building.reduceNeededResource(stack, 1);
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if ((job.getWorkOrder() == null && structurePlacer != null) || (structurePlacer != null && !structurePlacer.getB().hasBluePrint()))
        {
            if (job.hasWorkOrder())
            {
                job.getWorkOrder().clearBlueprint();
                job.getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
            }
            job.setWorkOrder(null);
            resetCurrentStructure();
            building.cancelAllRequestsOfCitizenOrBuilding(worker.getCitizenData());
            building.cancelAllRequestsOfCitizenOrBuilding(null);
            building.setProgressPos(null, BuildingProgressStage.CLEAR);
            return true;
        }
        return job.getWorkOrder() != null && (!WorldUtil.isBlockLoaded(world, job.getWorkOrder().getLocation()));
    }

    @Override
    @Nullable
    public ItemStack getTotalAmount(@Nullable final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return null;
        }
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        final AbstractBuildingStructureBuilder buildingWorker = building;
        BuildingBuilderResource resource = buildingWorker.getNeededResources().get(stack.getDescriptionId() + "-" + hashCode);

        if (resource == null)
        {
            requestMaterials();
            resource = buildingWorker.getNeededResources().get(stack.getDescriptionId() + "-" + hashCode);
        }

        if (resource == null)
        {
            return stack;
        }

        final ItemStack resStack = new ItemStack(resource.getItem(), Math.min(STACKSIZE, resource.getAmount()));
        resStack.setTag(resource.getItemStack().getTag());
        return resStack;
    }

    @Override
    public void handleSpecificCancelActions()
    {
        building.getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
        job.setWorkOrder(null);
    }
}
