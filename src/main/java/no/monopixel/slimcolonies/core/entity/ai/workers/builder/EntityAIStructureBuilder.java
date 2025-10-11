package no.monopixel.slimcolonies.core.entity.ai.workers.builder;

import com.ldtteam.structurize.placement.StructurePlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestState;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.IDeliverable;
import no.monopixel.slimcolonies.api.colony.workorders.IWorkOrder;
import no.monopixel.slimcolonies.api.colony.workorders.WorkOrderType;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.MessageUtils;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.api.util.Tuple;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import no.monopixel.slimcolonies.core.colony.jobs.JobBuilder;
import no.monopixel.slimcolonies.core.colony.workorders.WorkOrderBuilding;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractEntityAIStructureWithWorkOrder;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.BuildingProgressStage;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import no.monopixel.slimcolonies.core.SlimColonies;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.SlimColoniesAdvancedPathNavigate;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs.PathJobMoveCloseToXNearY;
import no.monopixel.slimcolonies.core.entity.pathfinding.pathresults.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.TICKS_SECOND;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEMS_SCAVENGED;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.COREMOD_ENTITY_BUILDER_MANUAL_SUFFIX;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

    /**
     * Current goto path
     */
    PathResult gotoPath = null;

    /**
     * Timestamp of the last scavenge check (in game ticks).
     */
    private long lastScavengeCheck = 0;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
            new AITarget(IDLE, START_WORKING, 10),
            new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Override to add scavenging behavior while waiting for requests.
     *
     * @return NEEDS_ITEM state.
     */
    @Override
    @NotNull
    protected IAIState waitForRequests()
    {
        final IAIState result = super.waitForRequests();

        // Check if scavenging is enabled
        final int intervalMinutes = SlimColonies.getConfig().getServer().builderScavengingIntervalMinutes.get();
        if (intervalMinutes == 0)
        {
            return result;
        }

        // Convert minutes to ticks with ±20% randomization
        // 1 minute = 60 seconds * 20 ticks/second = 1200 ticks
        final int baseTicks = intervalMinutes * 60 * 20;
        final double randomFactor = 0.8 + (worker.getRandom().nextDouble() * 0.4); // 0.8 to 1.2
        final long intervalTicks = (long) (baseTicks * randomFactor);

        final long currentTime = world.getGameTime();
        if (currentTime - lastScavengeCheck >= intervalTicks)
        {
            lastScavengeCheck = currentTime;
            final boolean scavengedSomething = tryScavengeForRequests();

            if (scavengedSomething && !building.hasOpenSyncRequest(worker.getCitizenData()))
            {
                return START_BUILDING;
            }
        }

        return result;
    }

    /**
     * Attempt to scavenge items for the builder's smallest request.
     * Builders can "find" small amounts (1-5 items) of needed materials while idle.
     *
     * @return true if items were scavenged, false otherwise
     */
    private boolean tryScavengeForRequests()
    {
        if (worker == null || worker.getCitizenData() == null || building == null)
        {
            return false;
        }

        if (!building.hasOpenSyncRequest(worker.getCitizenData()))
        {
            return false;
        }

        final List<IRequest<?>> allRequests = building.getOpenRequestsOfCitizenOrBuilding(
            worker.getCitizenData().getId(),
            request -> request.getRequest() instanceof IDeliverable
        );

        if (allRequests.isEmpty())
        {
            return false;
        }

        final var playerResolver = building.getColony().getRequestManager().getPlayerResolver();
        final var retryingResolver = building.getColony().getRequestManager().getRetryingRequestResolver();

        final List<IRequest<?>> requestsWeNeed = new ArrayList<>();
        for (final IRequest<?> request : allRequests)
        {
            try
            {
                final var assignedResolver = building.getColony().getRequestManager().getResolverForRequest(request.getId());
                if (assignedResolver != playerResolver && assignedResolver != retryingResolver)
                {
                    continue;
                }
            }
            catch (IllegalArgumentException e)
            {
                continue;
            }

            final IDeliverable deliverable = (IDeliverable) request.getRequest();
            final List<ItemStack> displayStacks = request.getDisplayStacks();

            if (displayStacks.isEmpty())
            {
                continue;
            }

            final ItemStack stackToCheck = displayStacks.get(0);
            final int currentCount = InventoryUtils.getItemCountInItemHandler(
                worker.getCitizenData().getInventory(),
                stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, stackToCheck)
            );

            final int stillNeeded = deliverable.getCount() - currentCount;

            if (stillNeeded > 0)
            {
                requestsWeNeed.add(request);
            }
        }

        if (requestsWeNeed.isEmpty())
        {
            return false;
        }

        requestsWeNeed.sort((a, b) -> {
            final IDeliverable delivA = (IDeliverable) a.getRequest();
            final IDeliverable delivB = (IDeliverable) b.getRequest();
            return Integer.compare(delivA.getCount(), delivB.getCount());
        });

        final IRequest<?> smallestRequest = requestsWeNeed.get(0);
        final IDeliverable deliverable = (IDeliverable) smallestRequest.getRequest();
        final List<ItemStack> displayStacks = smallestRequest.getDisplayStacks();
        final ItemStack stackToCheck = displayStacks.get(0);

        final int currentCount = InventoryUtils.getItemCountInItemHandler(
            worker.getCitizenData().getInventory(),
            stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, stackToCheck)
        );
        final int stillNeeded = deliverable.getCount() - currentCount;

        final int randomAmount = 1 + worker.getRandom().nextInt(5);
        final int scavengeAmount = Math.min(stillNeeded, randomAmount);

        final ItemStack stackToGive = stackToCheck.copy();
        stackToGive.setCount(scavengeAmount);

        final ItemStack remainingStack = InventoryUtils.addItemStackToItemHandlerWithResult(
            worker.getCitizenData().getInventory(),
            stackToGive
        );

        if (ItemStackUtils.isEmpty(remainingStack) || remainingStack.getCount() < scavengeAmount)
        {
            final int actuallyScavenged = scavengeAmount - (ItemStackUtils.isEmpty(remainingStack) ? 0 : remainingStack.getCount());
            final int newTotalCount = currentCount + actuallyScavenged;

            StatsUtil.trackStatByStack(building, ITEMS_SCAVENGED, stackToGive, actuallyScavenged);

            // Use overruleRequest with partial stack to let the request system handle partial fulfillment
            final ItemStack partialStack = stackToCheck.copy();
            partialStack.setCount(actuallyScavenged);
            building.getColony().getRequestManager().overruleRequest(smallestRequest.getId(), partialStack);

            Log.getLogger().info(
                "[{}] Scavenged {}x {} ({}/{} needed)",
                worker.getName().getString(),
                actuallyScavenged,
                stackToGive.getHoverName().getString(),
                newTotalCount,
                deliverable.getCount()
            );

            return true;
        }

        return false;
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!job.hasWorkOrder())
        {
            building.setProgressPos(null, BuildingProgressStage.CLEAR);
            worker.getCitizenData().setStatusPosition(null);
            return false;
        }

        final IWorkOrder wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            building.setProgressPos(null, null);
            worker.getCitizenData().setStatusPosition(null);
            return false;
        }

        final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getLocation());
        if (building == null && wo instanceof WorkOrderBuilding && wo.getWorkOrderType() != WorkOrderType.REMOVE)
        {
            job.complete();
            return false;
        }

        return true;
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobBuilder, BuildingBuilder> structure)
    {
        if (job.getWorkOrder().getIteratorType().isEmpty())
        {
            final String mode = BuilderModeSetting.getActualValue(building);
            job.getWorkOrder().setIteratorType(mode);
        }

        structurePlacer = new Tuple<>(new StructurePlacer(structure, job.getWorkOrder().getIteratorType()), structure);
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return !checkForWorkOrder();
    }

    private IAIState startWorkingAtOwnBuilding()
    {
        if (!walkToBuilding())
        {
            return getState();
        }
        return LOAD_STRUCTURE;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (building.getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && job.getWorkOrder() != null && job.getWorkOrder().getWorkOrderType() == WorkOrderType.BUILD)
        {
            final BlockPos buildingPos = job.getWorkOrder().getLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                WorldUtil.getEntitiesWithinBuilding(world, Monster.class, building, null).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!building.hasPurgedMobsToday())
        {
            killMobs();
            building.setPurgedMobsToday(true);
        }
    }

    @Override
    protected boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        return mineBlock(blockToMine, safeStand, true, !IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)), null);
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (workFrom != null && workFrom.getX() == currentBlock.getX() && workFrom.getZ() == currentBlock.getZ() && workFrom.getY() >= currentBlock.getY())
        {
            // Reset working position when standing ontop
            workFrom = null;
        }

        if (workFrom == null)
        {
            if (gotoPath == null || gotoPath.isCancelled())
            {
                final PathJobMoveCloseToXNearY pathJob = new PathJobMoveCloseToXNearY(world,
                    currentBlock,
                    job.getWorkOrder().getLocation(),
                    4,
                    worker);
                gotoPath = ((SlimColoniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(pathJob, currentBlock, 1.0, false);
                pathJob.getPathingOptions().dropCost = 200;
                pathJob.extraNodes = 0;
            }
            else if (gotoPath.isDone())
            {
                if (gotoPath.getPath() != null)
                {
                    workFrom = gotoPath.getPath().getTarget();
                }
                gotoPath = null;
            }

            if (prevBlockPosition != null)
            {
                return BlockPosUtil.dist(prevBlockPosition, currentBlock) <= 10;
            }
            return false;
        }

        if (!walkToSafePos(workFrom))
        {
            // Something might have changed, new wall and we can't reach the position anymore. Reset workfrom if stuck.
            if (worker.getNavigation() instanceof SlimColoniesAdvancedPathNavigate pathNavigate && pathNavigate.getStuckHandler().getStuckLevel() > 0)
            {
                workFrom = null;
            }
            return false;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), currentBlock) > 5)
        {
            if (BlockPosUtil.dist(workFrom, job.getWorkOrder().getLocation()) < 100)
            {
                prevBlockPosition = currentBlock;
                workFrom = null;
                return true;
            }
            workFrom = null;
            return false;
        }

        prevBlockPosition = currentBlock;
        return true;
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public int getBlockMiningTime(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        return (int) (super.getBlockMiningTime(state, pos) * SPEED_BUFF_0);
    }

    /**
     * Calculates after how many actions the AI should dump its inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    @Override
    protected void sendCompletionMessage(final IWorkOrder wo)
    {
        super.sendCompletionMessage(wo);

        final BlockPos position = wo.getLocation();
        boolean showManualSuffix = false;
        if (building.getManualMode())
        {
            showManualSuffix = true;
            for (final IWorkOrder workorder : building.getColony().getWorkManager().getWorkOrders().values())
            {
                if (workorder.getID() != wo.getID() && building.getID().equals(workorder.getClaimedBy()))
                {
                    showManualSuffix = false;
                }
            }
        }

        final MutableComponent message = Component.translatable(
                wo.getWorkOrderType().getCompletionMessageID(),
                wo.getDisplayName(),
                BlockPosUtil.calcDirection(building.getColony().getCenter(), position).getLongText())
            .withStyle(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Component.translatable("message.positiondist",
                        position.getX(),
                        position.getY(),
                        position.getZ(),
                        (int) BlockPosUtil.dist(building.getColony().getCenter(), position))))))
            .withStyle(ChatFormatting.GREEN);

        if (showManualSuffix)
        {
            message.append(Component.translatable(COREMOD_ENTITY_BUILDER_MANUAL_SUFFIX));
        }

        MessageUtils.forCitizen(worker, message).sendTo(worker.getCitizenColonyHandler().getColonyOrRegister().getImportantMessageEntityPlayers());
    }

    @Override
    public boolean canGoIdle()
    {
        return !job.hasWorkOrder();
    }
}
