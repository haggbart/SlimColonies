package no.monopixel.slimcolonies.core.entity.ai.workers.production.agriculture;

import com.google.common.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.network.PacketDistributor;
import no.monopixel.slimcolonies.api.advancements.AdvancementTriggers;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.StackList;
import no.monopixel.slimcolonies.api.entity.ai.JobStatus;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.Tuple;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.blocks.BlockScarecrow;
import no.monopixel.slimcolonies.core.colony.buildingextensions.FarmField;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingFarmer;
import no.monopixel.slimcolonies.core.colony.interactionhandling.StandardInteraction;
import no.monopixel.slimcolonies.core.colony.jobs.JobFarmer;
import no.monopixel.slimcolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import no.monopixel.slimcolonies.core.network.messages.client.CompostParticleMessage;
import no.monopixel.slimcolonies.core.util.AdvancementUtils;
import no.monopixel.slimcolonies.core.util.citizenutils.CitizenItemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FARMING;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.BLOCK_BREAK_SOUND_RANGE;
import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;
import static no.monopixel.slimcolonies.api.util.constant.Constants.TICKS_SECOND;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.FARMER_FIELDS;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * Farmer AI class. Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer>
{
    /**
     * Return to chest after this amount of stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * The EXP Earned per harvest.
     */
    private static final double XP_PER_HARVEST = 0.5;

    /**
     * The maximum depth to search for a surface
     */
    private static final int MAX_DEPTH = 5;

    /**
     * Farming icon
     */
    private static final VisibleCitizenStatus FARMING_ICON =
        new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/farmer.png"), "no.monopixel.slimcolonies.gui.visiblestatus.farmer");

    /**
     * Changed after finished harvesting in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * If the farmer actually did any work on the field.
     */
    private boolean didWork = false;

    /**
     * Amount of time we skipped state already.
     */
    private int skippedState = 0;

    /**
     * Constructor for the Farmer. Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(@NotNull final JobFarmer job)
    {
        super(job);
        super.registerTargets(
            new AITarget(PREPARING, this::prepareForFarming, TICKS_SECOND),
            new AITarget(FARMER_HOE, this::workAtField, 5),
            new AITarget(FARMER_PLANT, this::workAtField, 5),
            new AITarget(FARMER_HARVEST, this::workAtField, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingFarmer> getExpectedBuildingClass()
    {
        return BuildingFarmer.class;
    }

    /**
     * Called to check when the InventoryShouldBeDumped.
     *
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory || job.getActionsDone() >= getActionRewardForCraftingSuccess())
        {
            shouldDumpInventory = false;
            return true;
        }
        return super.wantInventoryDumped();
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata((getState() == FARMER_PLANT || getState() == FARMER_HARVEST) ? RENDER_META_WORKING : "");
    }

    @Override
    protected IAIState decide()
    {
        IAIState state = super.decide();

        if (state == IDLE)
        {
            return PREPARING;
        }
        return state;
    }

    @Override
    public boolean hasWorkToDo()
    {
        return true;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    /**
     * Prepares the farmer for farming. Also requests the tools, the compost (if needed) and checks if the farmer has sufficient fields.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState prepareForFarming()
    {
        worker.getCitizenData().setJobStatus(JobStatus.IDLE);
        if (building == null || building.getBuildingLevel() < 1)
        {
            worker.getCitizenData().setJobStatus(JobStatus.STUCK);
            return PREPARING;
        }

        final BuildingExtensionsModule module = building.getFirstModuleOccurance(BuildingExtensionsModule.class);
        if (module.getOwnedExtensions().size() == building.getMaxBuildingLevel())
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(building.getColony(), AdvancementTriggers.MAX_FIELDS::trigger);
        }

        final int amountOfCompostInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, this::isCompost, 1);
        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);

        if (amountOfCompostInBuilding + amountOfCompostInInv <= 0
            && building.requestFertilizer()
            && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)))
        {
            final List<ItemStack> compostAbleItems = new ArrayList<>();
            compostAbleItems.add(new ItemStack(ModItems.compost, 1));
            compostAbleItems.add(new ItemStack(Items.BONE_MEAL, 1));
            worker.getCitizenData().createRequestAsync(new StackList(compostAbleItems, RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER, STACKSIZE, 1));
        }
        else if (amountOfCompostInInv <= 0 && amountOfCompostInBuilding > 0)
        {
            needsCurrently = new Tuple<>(this::isCompost, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        if (module.hasNoExtensions())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            worker.getCitizenData().setJobStatus(JobStatus.STUCK);
            return IDLE;
        }

        final IBuildingExtension fieldToWork = module.getExtensionToWorkOn();
        if (fieldToWork instanceof FarmField farmField)
        {
            if (checkForToolOrWeapon(ModEquipmentTypes.hoe.get()))
            {
                worker.getCitizenData().setJobStatus(JobStatus.STUCK);
                return PREPARING;
            }
            worker.getCitizenData().setVisibleStatus(FARMING_ICON);
            worker.getCitizenData().setJobStatus(JobStatus.WORKING);

            // TEMPORARY: Force harvest mode for testing rice
            if (farmField.isWaterCrop())
            {
                Log.getLogger().info("[FARMER] TEMP: Forcing harvest check for water crop");
                if (checkIfShouldExecute(farmField, pos -> this.findHarvestableSurface(pos, farmField) != null))
                {
                    return FARMER_HARVEST;
                }
            }

            if (farmField.getFieldStage() == FarmField.Stage.PLANTED && checkIfShouldExecute(farmField, pos -> this.findHarvestableSurface(pos, farmField) != null))
            {
                return FARMER_HARVEST;
            }
            else if (farmField.getFieldStage() == FarmField.Stage.HOED)
            {
                return canGoPlanting(farmField);
            }
            else if (farmField.getFieldStage() == FarmField.Stage.EMPTY)
            {
                // Skip hoeing for water crops like rice
                if (farmField.isWaterCrop())
                {
                    farmField.nextState(); // Advance from EMPTY to HOED
                    return PREPARING; // Re-evaluate in next tick
                }

                if (checkIfShouldExecute(farmField, pos -> this.findHoeableSurface(pos, farmField) != null))
                {
                    return FARMER_HOE;
                }
            }
            farmField.nextState();
            // TEMPORARY: Make farmer never idle for testing
            if (++skippedState >= 0)  // Changed from >= 4
            {
                skippedState = 0;
                didWork = true;
                module.resetCurrentExtension();
            }
            return PREPARING;  // Changed from IDLE - immediately recheck
        }
        else if (fieldToWork != null)
        {
            Log.getLogger().warn("Farmer found non-FarmField extension: {}", fieldToWork.getClass());
        }
        return IDLE;
    }

    /**
     * Check if itemStack can be used as compost.
     *
     * @param itemStack the stack to check.
     * @return true if so.
     */
    private boolean isCompost(final ItemStack itemStack)
    {
        if (itemStack.getItem() == ModItems.compost)
        {
            return true;
        }
        return itemStack.getItem() == Items.BONE_MEAL;
    }

    /**
     * Handles the offset of the field for the farmer. Checks if the field needs a certain operation checked with a given predicate.
     *
     * @param farmField the field object.
     * @param predicate the predicate to test.
     * @return true if a harvestable crop was found.
     */
    private boolean checkIfShouldExecute(@NotNull final FarmField farmField, @NotNull final Predicate<BlockPos> predicate)
    {
        BlockPos position;
        do
        {
            building.setWorkingOffset(nextValidCell(farmField));
            if (building.getWorkingOffset() == null)
            {
                return false;
            }

            position = farmField.getPosition().below().south(building.getWorkingOffset().getZ()).east(building.getWorkingOffset().getX());
        }
        while (!predicate.test(position));

        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param farmField the field to plant.
     * @return the next AI state.
     */
    private IAIState canGoPlanting(@NotNull final FarmField farmField)
    {
        if (farmField.getSeed() == null)
        {
            return PREPARING;
        }

        final ItemStack seeds = farmField.getSeed();
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(seeds.getItem());
        if (slot != -1)
        {
            return FARMER_PLANT;
        }

        if (!walkToBuilding())
        {
            return PREPARING;
        }

        final ItemStack seedRequest = seeds.copy();
        seedRequest.setCount(seeds.getMaxStackSize());
        if (!checkIfRequestForItemExistOrCreateAsync(seedRequest, seedRequest.getMaxStackSize(), 1))
        {
            farmField.nextState();
        }
        return PREPARING;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return position of hoeable surface or null if there is not one
     */
    private BlockPos findHoeableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }
        final BlockState blockState = world.getBlockState(position);
        final BlockState aboveState = world.getBlockState(position.above());

        if (farmField.isNoPartOfField(world, position)
            || (aboveState.getBlock() instanceof CropBlock)
            || (aboveState.getBlock() instanceof BlockScarecrow)
            || (!blockState.is(BlockTags.DIRT) && !(blockState.getBlock() instanceof FarmBlock))
            || isRightFarmLandForCrop(blockState)
        )
        {
            return null;
        }
        // Don't destroy water blocks (needed for rice farming)
        if (aboveState.canBeReplaced() && !(aboveState.getBlock() instanceof LiquidBlock))
        {
            world.destroyBlock(position.above(), true);
        }

        if (!isRightFarmLandForCrop(blockState))
        {
            return position;
        }

        final BlockHitResult blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, position, false);
        final UseOnContext useOnContext = new UseOnContext(world,
            null,
            InteractionHand.MAIN_HAND,
            getInventory().getStackInSlot(InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.hoe.get(), 0, Integer.MAX_VALUE)),
            blockHitResult);
        final BlockState toolModifiedState = blockState.getToolModifiedState(useOnContext, ToolActions.HOE_TILL, true);
        if (toolModifiedState == null || !toolModifiedState.is(Blocks.FARMLAND))
        {
            return null;
        }

        return position;
    }

    /**
     * Finds the position of the surface at the specified position.
     * Position is always relative to scarecrow Y level, so only searches downward.
     *
     * @param position the location to begin the search (scarecrow.Y - 1)
     * @return the position of the surface block or null if it can't be found
     */
    private BlockPos getSurfacePos(final BlockPos position)
    {
        if (!WorldUtil.isBlockLoaded(world, position))
        {
            return null;
        }

        BlockState state = world.getBlockState(position);
        Block block = state.getBlock();

        // If starting on crop/stem, skip down through them to find surface
        if (block instanceof CropBlock || block instanceof StemBlock)
        {
            BlockPos checkPos = position.below();
            for (int i = 0; i < MAX_DEPTH; i++)
            {
                state = world.getBlockState(checkPos);
                block = state.getBlock();

                if (!(block instanceof CropBlock) && !(block instanceof StemBlock))
                {
                    // Hit the block below crops - check if it's valid surface
                    if (state.isSolid() && !(block instanceof PumpkinBlock) && !(block instanceof MelonBlock) && !(block instanceof WebBlock))
                    {
                        return checkPos;
                    }
                    // Continue searching downward
                    break;
                }
                checkPos = checkPos.below();
            }
            // Search down from where crops ended
            return searchDownForSurface(checkPos.below());
        }

        // If already at valid solid surface, return it
        if (state.isSolid() && !(block instanceof PumpkinBlock) && !(block instanceof MelonBlock) && !(block instanceof WebBlock))
        {
            return position;
        }

        // Starting in air/water - search downward to find surface
        return searchDownForSurface(position.below());
    }

    /**
     * Search downward for solid surface block.
     * Only searches down since field surfaces are always at or below scarecrow level.
     */
    private BlockPos searchDownForSurface(final BlockPos startPos)
    {
        BlockPos checkPos = startPos;
        for (int i = 0; i < MAX_DEPTH; i++)
        {
            final BlockState state = world.getBlockState(checkPos);
            final Block block = state.getBlock();

            if (state.isSolid() && !(block instanceof PumpkinBlock) && !(block instanceof MelonBlock) && !(block instanceof WebBlock))
            {
                return checkPos;
            }
            checkPos = checkPos.below();
        }
        return null;
    }

    /**
     * Fetch the next available block within the field. Uses mathematical quadratic equations to determine the coordinates by an index. Considers max radii set in the field gui.
     *
     * @return the new offset position
     */
    protected BlockPos nextValidCell(FarmField farmField)
    {
        int ring, ringCell, x, z;
        Direction facing;

        if (building.getWorkingOffset() == null)
        {
            building.setCell(-1);
        }

        do
        {
            if (building.setCell(building.getCell() + 1) == getLargestCell(farmField))
            {
                return null;
            }
            ring = (int) Math.floor((Math.sqrt(building.getCell() + 1D) + 1) / 2.0);
            ringCell = building.getCell() - (int) (4 * Math.pow(ring - 1D, 2) + 4 * (ring - 1));
            facing = Direction.from2DDataValue(Math.floorDiv(ringCell, 2 * ring));


            if (facing.getAxis() == Direction.Axis.Z)
            {
                x = (facing == Direction.NORTH ? -1 : 1) * (ring - (ringCell % (2 * ring)));
                z = (facing == Direction.NORTH ? -1 : 1) * ring;
            }
            else
            {
                x = (facing == Direction.WEST ? -1 : 1) * ring;
                z = (facing == Direction.EAST ? -1 : 1) * (ring - (ringCell % (2 * ring)));
            }
        }
        while (
            -z > farmField.getRadius(Direction.NORTH)
                || x > farmField.getRadius(Direction.EAST)
                || z > farmField.getRadius(Direction.SOUTH)
                || -x > farmField.getRadius(Direction.WEST)
        );

        return new BlockPos(x, 0, z);
    }

    protected int getLargestCell(FarmField farmField)
    {
        return (int) Math.pow(farmField.getMaxRadius() * 2D + 1D, 2);
    }

    /**
     * This (re)initializes a field. Checks the block above to see if it is a plant, if so, breaks it. Then tills.
     *
     * @return the next state to go into.
     */
    private IAIState workAtField()
    {
        final BuildingExtensionsModule module = building.getFirstModuleOccurance(BuildingExtensionsModule.class);
        final IBuildingExtension field = module.getCurrentExtension();

        worker.getCitizenData().setVisibleStatus(FARMING_ICON);
        if (field instanceof FarmField farmField)
        {
            if (building.getWorkingOffset() != null)
            {
                final BlockPos position = farmField.getPosition().below().south(building.getWorkingOffset().getZ()).east(building.getWorkingOffset().getX());

                // Still moving to the block
                if (!walkToSafePos(position.above()))
                {
                    return getState();
                }

                switch ((AIWorkerState) getState())
                {
                    case FARMER_HOE ->
                    {
                        if (!hoeIfAble(position, farmField))
                        {
                            didWork = true;
                            return getState();
                        }
                    }
                    case FARMER_PLANT ->
                    {
                        if (!tryToPlant(farmField, position))
                        {
                            didWork = true;
                            return PREPARING;
                        }
                    }
                    case FARMER_HARVEST ->
                    {
                        if (!harvestIfAble(position, farmField))
                        {
                            didWork = true;
                            return getState();
                        }
                    }
                    default ->
                    {
                        return PREPARING;
                    }
                }
                building.setPrevPos(position);
            }

            building.setWorkingOffset(nextValidCell(farmField));
            if (building.getWorkingOffset() == null)
            {
                shouldDumpInventory = true;
                farmField.nextState();
                module.markDirty();
                if (didWork || ++skippedState >= 4)
                {
                    module.resetCurrentExtension();
                    skippedState = 0;
                }
                didWork = false;
                building.setPrevPos(null);
                return IDLE;
            }
        }
        else
        {
            return IDLE;
        }
        return getState();
    }

    /**
     * Checks if we can hoe, and does so if we can.
     *
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return true if the farmer should move on.
     */
    private boolean hoeIfAble(BlockPos position, final FarmField farmField)
    {
        position = findHoeableSurface(position, farmField);
        if (position != null && !checkForToolOrWeapon(ModEquipmentTypes.hoe.get()))
        {
            if (mineBlock(position.above()))
            {
                equipHoe();
                worker.swing(worker.getUsedItemHand());
                createCorrectFarmlandForSeed(position);
                CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);

                final var colony = worker.getCitizenColonyHandler().getColonyOrRegister();
                colony.getStatisticsManager().increment(LAND_TILLED, colony.getDay());

                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Create the correct farmland at a given position.
     *
     * @param pos the position.
     */
    private void createCorrectFarmlandForSeed(final BlockPos pos)
    {
        world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
    }

    private boolean isRightFarmLandForCrop(final BlockState blockState)
    {
        return blockState.getBlock() instanceof FarmBlock;
    }

    /**
     * Tries to harvest a crop. Attempts right-click first, then falls back to breaking.
     *
     * @param position the block to harvest
     * @param farmField the farm field being harvested
     * @return true if we harvested or not supposed to
     */
    private boolean harvestIfAble(BlockPos position, final FarmField farmField)
    {
        position = findHarvestableSurface(position, farmField);
        if (position != null)
        {
            BlockPos cropPos = position.above();

            // Check if this is a 2-block tall water crop (like rice) - harvest the top part
            if (farmField.isWaterCrop())
            {
                final BlockState aboveCropState = world.getBlockState(cropPos.above());

                // If the block above is a mature CropBlock, harvest it instead of the bottom
                if (aboveCropState.getBlock() instanceof CropBlock cropAbove && cropAbove.isMaxAge(aboveCropState))
                {
                    cropPos = cropPos.above(); // Harvest the top block instead
                }
            }

            final InteractionResult useResult = useBlock(cropPos);

            boolean harvestSuccess;
            if (useResult.consumesAction())
            {
                trackItemsFromRightClickHarvest(cropPos);
                harvestSuccess = true;
            }
            else
            {
                harvestSuccess = mineBlock(cropPos);
            }

            if (harvestSuccess)
            {
                final var colony = worker.getCitizenColonyHandler().getColonyOrRegister();
                colony.getStatisticsManager().increment(CROPS_HARVESTED, colony.getDay());
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Tracks items spawned by right-click harvesting for statistics.
     *
     * @param cropPos the position where the crop was harvested
     */
    private void trackItemsFromRightClickHarvest(final BlockPos cropPos)
    {
        world.getServer().execute(() -> {
            final List<ItemEntity> itemEntities = world.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(cropPos).inflate(2.0)
            );

            if (!itemEntities.isEmpty())
            {
                final List<ItemStack> harvestedItems = new ArrayList<>();
                for (final ItemEntity itemEntity : itemEntities)
                {
                    if (itemEntity.getAge() <= 2)
                    {
                        harvestedItems.add(itemEntity.getItem().copy());
                    }
                }

                if (!harvestedItems.isEmpty())
                {
                    for (final ItemStack stack : harvestedItems)
                    {
                        building.getModule(STATS_MODULE).incrementBy(
                            ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(),
                            stack.getCount()
                        );
                    }
                }
            }
        });
    }

    @Override
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        super.onBlockDropReception(blockDrops);
        for (final ItemStack stack : blockDrops)
        {
            building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(), stack.getCount());
        }
    }

    /**
     * Try to plant the field at a certain position.
     *
     * @param farmField the field to try to plant.
     * @param position  the position to try.
     * @return the next state to go to.
     */
    private boolean tryToPlant(final FarmField farmField, BlockPos position)
    {
        position = findPlantableSurface(position, farmField);
        if (position == null)
        {
            return true;
        }
        return plantCrop(farmField.getSeed(), position);
    }

    /**
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, getHoeSlot());
    }

    /**
     * Checks if the ground should be planted.
     *
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return position of plantable surface or null
     */
    private BlockPos findPlantableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }

        final BlockState blockState = world.getBlockState(position);
        final BlockState aboveState = world.getBlockState(position.above());

        // Check all conditions in one go
        if (farmField.isNoPartOfField(world, position)
            || aboveState.getBlock() instanceof CropBlock
            || aboveState.getBlock() instanceof StemBlock
            || blockState.getBlock() instanceof BlockScarecrow
            || (farmField.isWaterCrop() ? !(aboveState.getBlock() instanceof LiquidBlock) : !aboveState.isAir())
            || (!isRightFarmLandForCrop(blockState) && !(farmField.isWaterCrop() && blockState.is(BlockTags.DIRT))))
        {
            return null;
        }

        return position;
    }

    /**
     * Plants the crop at a given location.
     *
     * @param item     the crop.
     * @param position the location.
     * @return true if successful.
     */
    private boolean plantCrop(final ItemStack item, @NotNull final BlockPos position)
    {
        if (item == null || item.isEmpty())
        {
            return false;
        }
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(item.getItem());
        if (slot == -1)
        {
            return false;
        }

        if (item.getItem() instanceof BlockItem blockItem)
        {
            @NotNull final Item seed = item.getItem();
            if ((seed == Items.MELON_SEEDS || seed == Items.PUMPKIN_SEEDS) && building.getPrevPos() != null && !world.isEmptyBlock(building.getPrevPos().above()))
            {
                return true;
            }

            world.setBlockAndUpdate(position.above(), blockItem.getBlock().defaultBlockState());
            getInventory().extractItem(slot, 1, false);
        }
        return true;
    }

    /**
     * Checks if the crop should be harvested.
     *
     * @param position the position to check.
     * @param farmField the farm field being checked
     * @return position of harvestable block or null
     */
    private BlockPos findHarvestableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }
        BlockState state = world.getBlockState(position.above());
        Block block = state.getBlock();

        if (block == Blocks.PUMPKIN || block == Blocks.MELON)
        {
            return position;
        }

        if (block instanceof CropBlock)
        {
            @NotNull CropBlock crop = (CropBlock) block;
            if (crop.isMaxAge(state))
            {
                return position;
            }
            final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);
            if (amountOfCompostInInv == 0)
            {
                return null;
            }

            if (InventoryUtils.shrinkItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost))
            {
                Network.getNetwork().sendToPosition(new CompostParticleMessage(position.above()),
                    new PacketDistributor.TargetPoint(position.getX(), position.getY(), position.getZ(), BLOCK_BREAK_SOUND_RANGE, world.dimension()));
                crop.growCrops(world, position.above(), state);
                state = world.getBlockState(position.above());
                block = state.getBlock();
                if (block instanceof CropBlock)
                {
                    crop = (CropBlock) block;
                }
                else
                {
                    return null;
                }
            }
            return crop.isMaxAge(state) ? position : null;
        }

        // For 2-block tall water crops (like rice), check the block above
        if (farmField.isWaterCrop())
        {
            BlockState stateAbove = world.getBlockState(position.above().above());
            Block blockAbove = stateAbove.getBlock();

            // Check if the top block is a CropBlock
            if (blockAbove instanceof CropBlock)
            {
                CropBlock cropAbove = (CropBlock) blockAbove;
                if (cropAbove.isMaxAge(stateAbove))
                {
                    return position;
                }
            }
        }

        return null;
    }

    @Override
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        final double increaseCrops = worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(FARMING);
        if (increaseCrops == 0)
        {
            return drops;
        }

        final List<ItemStack> newDrops = new ArrayList<>();
        for (final ItemStack stack : drops)
        {
            final ItemStack drop = stack.copy();
            if (worker.getRandom().nextDouble() < increaseCrops)
            {
                drop.setCount(drop.getCount() * 2);
            }
            newDrops.add(drop);
        }

        return newDrops;
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    /**
     * Get's the slot in which the hoe is in.
     *
     * @return slot number
     */
    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.hoe.get(), 0, Integer.MAX_VALUE);
    }

    /**
     * Returns the farmer's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }

    @Override
    public boolean canGoIdle()
    {
        if (building.getModule(FARMER_FIELDS).getExtensionToWorkOn() == null)
        {
            return !super.hasWorkToDo();
        }

        return false;
    }
}
