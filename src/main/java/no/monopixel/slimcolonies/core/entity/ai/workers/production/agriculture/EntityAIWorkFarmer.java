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
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.util.*;
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
 * Farmer AI class.
 */
public class EntityAIWorkFarmer extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer>
{
    private static final double XP_PER_HARVEST = 0.5;
    private static final int    MAX_DEPTH      = 5;

    private static final VisibleCitizenStatus FARMING_ICON =
        new VisibleCitizenStatus(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/icons/work/farmer.png"), "no.monopixel.slimcolonies.gui.visiblestatus.farmer");

    /**
     * Set after field scan completes to trigger inventory dump.
     */
    private boolean shouldDumpInventory = false;

    private boolean                        didWork     = false;
    private IBuildingExtension.ExtensionId lastFieldId = null;  // Track field changes

    public EntityAIWorkFarmer(@NotNull final JobFarmer job)
    {
        super(job);
        super.registerTargets(
            new AITarget(PREPARING, this::prepareForFarming, TICKS_SECOND),
            new AITarget(FARMER_HARVEST, this::workAtField, 5)  // Single state for all field work
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingFarmer> getExpectedBuildingClass()
    {
        return BuildingFarmer.class;
    }

    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory)
        {
            shouldDumpInventory = false;
            return true;
        }
        return super.wantInventoryDumped();
    }

    /**
     * Override to disable action-counter-based dumps.
     * Farmer only dumps when inventory is full or field scan completes.
     *
     * @return Integer.MAX_VALUE to effectively disable action counter
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getState() == FARMER_HARVEST ? RENDER_META_WORKING : "");
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

        // If null, all fields are on cooldown - go idle and wait
        if (fieldToWork == null)
        {
            Log.getLogger().info("Farmer {} all fields on cooldown, going idle", worker.getName().getString());
            return IDLE;
        }

        if (fieldToWork instanceof FarmField farmField)
        {
            if (checkForToolOrWeapon(ModEquipmentTypes.hoe.get()))
            {
                worker.getCitizenData().setJobStatus(JobStatus.STUCK);
                return PREPARING;
            }
            worker.getCitizenData().setVisibleStatus(FARMING_ICON);
            worker.getCitizenData().setJobStatus(JobStatus.WORKING);

            final ItemStack seeds = farmField.getSeed();
            if (seeds != null && !seeds.isEmpty())
            {
                final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(seeds.getItem());
                if (slot == -1)
                {
                    if (!walkToBuilding())
                    {
                        return PREPARING;
                    }
                    final ItemStack seedRequest = seeds.copy();
                    seedRequest.setCount(seeds.getMaxStackSize());
                    checkIfRequestForItemExistOrCreateAsync(seedRequest, seedRequest.getMaxStackSize(), 1);
                    return PREPARING;
                }
            }

            return FARMER_HARVEST;
        }
        return PREPARING;
    }

    private boolean isCompost(final ItemStack itemStack)
    {
        if (itemStack.getItem() == ModItems.compost)
        {
            return true;
        }
        return itemStack.getItem() == Items.BONE_MEAL;
    }

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

        if (farmField.isWaterCrop())
        {
            if (!(aboveState.getBlock() instanceof LiquidBlock))
            {
                return null;
            }
        }
        else
        {
            if (aboveState.getBlock() instanceof LiquidBlock)
            {
                return null;
            }
            // Clear weeds/grass if needed
            if (aboveState.canBeReplaced())
            {
                world.destroyBlock(position.above(), true);
            }
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

        if (block instanceof CropBlock || block instanceof StemBlock)
        {
            BlockPos checkPos = position.below();
            for (int i = 0; i < MAX_DEPTH; i++)
            {
                state = world.getBlockState(checkPos);
                block = state.getBlock();

                if (!(block instanceof CropBlock) && !(block instanceof StemBlock))
                {
                    if (state.isSolid() && !(block instanceof PumpkinBlock) && !(block instanceof MelonBlock) && !(block instanceof WebBlock))
                    {
                        return checkPos;
                    }
                    break;
                }
                checkPos = checkPos.below();
            }
            return searchDownForSurface(checkPos.below());
        }

        if (state.isSolid() && !(block instanceof PumpkinBlock) && !(block instanceof MelonBlock) && !(block instanceof WebBlock))
        {
            return position;
        }

        return searchDownForSurface(position.below());
    }

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

    private IAIState workAtField()
    {
        final BuildingExtensionsModule module = building.getFirstModuleOccurance(BuildingExtensionsModule.class);
        final IBuildingExtension field = module.getCurrentExtension();

        if (field != null && !field.getId().equals(lastFieldId))
        {
            lastFieldId = field.getId();
            Log.getLogger().info("Farmer {} switched to new field", worker.getName().getString());
        }

        worker.getCitizenData().setVisibleStatus(FARMING_ICON);
        if (field instanceof FarmField farmField)
        {
            if (building.getWorkingOffset() != null)
            {
                final BlockPos position = farmField.getPosition().below().south(building.getWorkingOffset().getZ()).east(building.getWorkingOffset().getX());

                if (!walkToSafePos(position.above()))
                {
                    return getState();
                }

                boolean workedThisBlock = false;

                if (harvestIfAble(position, farmField))
                {
                    workedThisBlock = true;
                }

                if (!farmField.isWaterCrop() && hoeIfAble(position, farmField))
                {
                    workedThisBlock = true;
                }

                if (tryToPlant(farmField, position))
                {
                    workedThisBlock = true;
                }

                if (workedThisBlock)
                {
                    didWork = true;
                }

                // Track previous position for melon/pumpkin spacing
                building.setPrevPos(position);
            }

            building.setWorkingOffset(nextValidCell(farmField));
            if (building.getWorkingOffset() == null)
            {
                // Field scan completed - rotate to next field
                shouldDumpInventory = true;
                module.markDirty();

                Log.getLogger().info("Farmer {} completed field (work done: {})", worker.getName().getString(), didWork);

                // Always rotate to next field after completing one pass
                module.resetCurrentExtension();
                didWork = false;
                building.setPrevPos(null);
                return PREPARING;
            }
        }
        else
        {
            return PREPARING;
        }
        return getState();
    }

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

    private void createCorrectFarmlandForSeed(final BlockPos pos)
    {
        world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
    }

    private boolean isRightFarmLandForCrop(final BlockState blockState)
    {
        return blockState.getBlock() instanceof FarmBlock;
    }

    private boolean harvestIfAble(BlockPos position, final FarmField farmField)
    {
        position = findHarvestableSurface(position, farmField);
        if (position != null)
        {
            BlockPos cropPos = position.above();

            if (farmField.isWaterCrop())
            {
                final BlockState aboveCropState = world.getBlockState(cropPos.above());
                if (aboveCropState.getBlock() instanceof CropBlock cropAbove && cropAbove.isMaxAge(aboveCropState))
                {
                    cropPos = cropPos.above();
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
                harvestSuccess = harvestCropInstantly(cropPos);
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
     * Instantly breaks a crop block and collects drops without mining delay.
     * Used for harvesting mature crops which should be instant.
     *
     * @param cropPos the position of the crop to harvest
     * @return true if successfully harvested
     */
    private boolean harvestCropInstantly(final BlockPos cropPos)
    {
        final BlockState cropState = world.getBlockState(cropPos);
        final Block cropBlock = cropState.getBlock();

        if (cropBlock instanceof AirBlock)
        {
            return true;
        }

        final ItemStack tool = worker.getMainHandItem();
        final int fortune = ItemStackUtils.getFortuneOf(tool);

        List<ItemStack> drops = BlockPosUtil.getBlockDrops(world, cropPos, fortune, tool, worker);
        drops = increaseBlockDrops(drops);

        for (final ItemStack item : drops)
        {
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, worker.getInventoryCitizen());
        }
        onBlockDropReception(drops);

        CitizenItemUtils.breakBlockWithToolInHand(worker, cropPos);

        worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
        this.incrementActionsDone();

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

    private boolean tryToPlant(final FarmField farmField, BlockPos position)
    {
        position = findPlantableSurface(position, farmField);
        if (position == null)
        {
            return true;
        }
        return plantCrop(farmField.getSeed(), position);
    }

    private void equipHoe()
    {
        CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, getHoeSlot());
    }

    private BlockPos findPlantableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }

        final BlockState blockState = world.getBlockState(position);
        final BlockState aboveState = world.getBlockState(position.above());

        if (farmField.isNoPartOfField(world, position)
            || aboveState.getBlock() instanceof CropBlock
            || aboveState.getBlock() instanceof StemBlock
            || blockState.getBlock() instanceof BlockScarecrow)
        {
            return null;
        }

        if (farmField.isWaterCrop())
        {
            // Water crops: above must be water/liquid, ground can be dirt OR farmland
            if (!(aboveState.getBlock() instanceof LiquidBlock)
                || (!blockState.is(BlockTags.DIRT) && !isRightFarmLandForCrop(blockState)))
            {
                return null;
            }
        }
        else
        {
            // Regular crops: above must be air, ground MUST be farmland
            if (!aboveState.isAir() || !isRightFarmLandForCrop(blockState))
            {
                return null;
            }
        }

        return position;
    }

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

        if (farmField.isWaterCrop())
        {
            BlockState stateAbove = world.getBlockState(position.above().above());
            Block blockAbove = stateAbove.getBlock();

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

    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.hoe.get(), 0, Integer.MAX_VALUE);
    }

    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }

    @Override
    public boolean canGoIdle()
    {
        final BuildingExtensionsModule module = building.getModule(FARMER_FIELDS);

        // If there are any fields at all, farmer should not idle
        if (!module.hasNoExtensions())
        {
            return false;
        }

        return !super.hasWorkToDo();
    }
}
