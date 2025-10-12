package no.monopixel.slimcolonies.apiimp.initializer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestUtils;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.ItemListModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.QuarryModule;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.*;
import no.monopixel.slimcolonies.core.colony.jobs.*;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import no.monopixel.slimcolonies.core.util.WorkerUtil;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.ItemStackUtils.ISFOOD;
import static no.monopixel.slimcolonies.api.util.ItemStackUtils.IS_COMPOST;
import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static no.monopixel.slimcolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_RESOLVER_NORMAL;
import static no.monopixel.slimcolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW;
import static no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules.RESTAURANT_MENU;
import static no.monopixel.slimcolonies.core.entity.ai.workers.crafting.EntityAIWorkSmelter.ORE_LIST;
import static no.monopixel.slimcolonies.core.entity.ai.workers.production.agriculture.EntityAIWorkFisherman.SUBOPTIMAL_POND_COMPLAINT_DISTANCE;
import static no.monopixel.slimcolonies.core.util.WorkerUtil.getLastLadder;
import static no.monopixel.slimcolonies.core.util.WorkerUtil.isThereCompostedLand;

/**
 * Class containing initializer for all the validator predicates.
 */
public class InteractionValidatorInitializer
{
    private static final String HOMELESSNESS = "homelessness";
    private static final String UNEMPLOYMENT = "unemployment";
    private static final String IDLEATJOB    = "idleatjob";

    /**
     * Init method called on startup.
     */
    public static void init()
    {
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FUEL),
            citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding()
                .hasModule(BuildingModules.ITEMLIST_FUEL)
                && citizen.getWorkBuilding().getModule(BuildingModules.ITEMLIST_FUEL).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE),
            citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding()
                .getModule(BuildingModules.FURNACE)
                .getFurnaces()
                .isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BETTER_FOOD),
            citizen -> citizen.getSaturation() == 0 && !citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BETTER_FOOD_CHILDREN),
            citizen -> citizen.getSaturation() == 0 && citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_RESTAURANT),
            citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getEntity().isPresent()
                && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingCook.class) == null
                && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == -1);

        InteractionValidatorRegistry.registerPosBasedPredicate(Component.translatable(COREMOD_JOB_DELIVERYMAN_CHESTFULL),
            (citizen, pos) ->
            {
                if (citizen.getJob() instanceof JobDeliveryman)
                {
                    final IColony colony = citizen.getColony();
                    if (colony != null)
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                        if (building != null)
                        {
                            final IItemHandler inv = building.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
                            if (inv != null)
                            {
                                return InventoryUtils.openSlotCount(inv) <= 0;
                            }
                        }
                    }
                }
                return false;
            });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
            cit -> {
                if (cit.getJob() instanceof JobDeliveryman && cit.getWorkBuilding() != null)
                {
                    return ((JobDeliveryman) cit.getJob()).findWareHouse() == null;
                }
                return false;
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_FREE_FIELDS),
            citizen -> citizen.getWorkBuilding() instanceof BuildingFarmer && citizen.getWorkBuilding().getModule(BuildingModules.FARMER_FIELDS).hasNoExtensions());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(INVALID_MINESHAFT),
            citizen -> citizen.getWorkBuilding() instanceof BuildingMiner && citizen.getJob() instanceof JobMiner && (
                ((BuildingMiner) citizen.getWorkBuilding()).getCobbleLocation() == null || ((BuildingMiner) citizen.getWorkBuilding()).getLadderLocation() == null));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST),
            citizen -> citizen.getWorkBuilding() != null && InventoryUtils.isBuildingFull(citizen.getWorkBuilding()));
        InteractionValidatorRegistry.registerPosBasedPredicate(
            Component.translatable(REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW), (citizen, pos) ->
            {
                final IBuilding workBuilding = citizen.getWorkBuilding();
                if (workBuilding != null)
                {
                    final IColony colony = citizen.getColony();
                    if (colony != null)
                    {
                        final Level world = colony.getWorld();
                        if (world != null)
                        {
                            return Integer.MAX_VALUE < WorkerUtil.getCorrectHarvestLevelForBlock(world.getBlockState(pos));
                        }
                    }
                }
                return false;
            });
        InteractionValidatorRegistry.registerTokenBasedPredicate(Component.translatable(REQUEST_RESOLVER_NORMAL),
            (citizen, token) -> {

                final IColony colony = citizen.getColony();
                if (colony != null)
                {
                    return RequestUtils.requestChainNeedsPlayer(token, citizen.getColony().getRequestManager());
                }
                return false;
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_ORE),
            citizen -> {
                if (citizen.getWorkBuilding() instanceof BuildingSmeltery)
                {
                    final List<ItemStorage> oreList =
                        ((BuildingSmeltery) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
                    for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres())
                    {
                        if (!oreList.contains(storage))
                        {
                            return true;
                        }
                    }
                }
                return false;
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(PUPIL_NO_CARPET),
            citizen -> citizen.getEntity().isPresent() && citizen.isChild() && citizen.getWorkBuilding() instanceof BuildingSchool
                && ((BuildingSchool) citizen.getWorkBuilding()).getRandomPlaceToSit() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(WATER_TOO_FAR),
            citizen -> citizen.getJob() instanceof JobFisherman && ((JobFisherman) citizen.getJob()).getPonds().isEmpty());

        InteractionValidatorRegistry.registerPosBasedPredicate(Component.translatable(SUBOPTIMAL_POND),
            (citizen, pos) ->
            {
                return citizen.getJob() instanceof JobFisherman && (BlockPosUtil.getDistance(citizen.getEntity().get().blockPosition(), pos) <= SUBOPTIMAL_POND_COMPLAINT_DISTANCE);
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FUEL),
            citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding()
                .hasModule(BuildingModules.ITEMLIST_FUEL) && citizen.getWorkBuilding().getModule(BuildingModules.ITEMLIST_FUEL).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FOOD),
            citizen -> {
                if (!(citizen.getWorkBuilding() instanceof BuildingCook))
                {
                    return false;
                }

                return citizen.getWorkBuilding().getModule(RESTAURANT_MENU).getMenu().isEmpty();
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NETHERMINER_NO_FOOD),
            citizen -> {
                if (!(citizen.getWorkBuilding() instanceof BuildingNetherWorker))
                {
                    return false;
                }

                return citizen.getWorkBuilding().getModule(RESTAURANT_MENU).getMenu().isEmpty();
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(SIFTER_NO_MESH),
            citizen -> {
                if (!(citizen.getWorkBuilding() instanceof BuildingSifter))
                {
                    return false;
                }
                return InventoryUtils.getItemCountInProvider(citizen.getWorkBuilding(), item -> item.is(ModTags.meshes)) <= 0 &&
                    InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), item -> item.is(ModTags.meshes)) <= 0;
            });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE),
            citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && citizen.getWorkBuilding().getModule(BuildingModules.FURNACE).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_HIVES),
            citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && ((BuildingBeekeeper) citizen.getWorkBuilding()).getHives().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_BEES),
            citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && citizen.getJob(JobBeekeeper.class).checkForBeeInteraction());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_WORKERS_TO_DRAIN_SET),
            citizen -> citizen.getWorkBuilding() instanceof BuildingEnchanter && ((BuildingEnchanter) citizen.getWorkBuilding()).getModule(BuildingModules.ENCHANTER_STATIONS)
                .getBuildingsToGatherFrom()
                .isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_PLANT_GROUND_FLORIST),
            citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ((BuildingFlorist) citizen.getWorkBuilding()).getPlantGround().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_FLOWERS_IN_CONFIG),
            citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ItemStackUtils.isEmpty(((BuildingFlorist) citizen.getWorkBuilding()).getFlowerToGrow()));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_COMPOST),
            citizen ->
            {
                final IBuilding buildingFlorist = citizen.getWorkBuilding();
                if (buildingFlorist instanceof BuildingFlorist && buildingFlorist.getColony().getWorld() != null)
                {
                    return InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), IS_COMPOST) == 0 && !isThereCompostedLand((BuildingFlorist) buildingFlorist,
                        buildingFlorist.getColony().getWorld());
                }
                return false;
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NEEDS_BETTER_HUT),
            citizen -> {

                final AbstractBuilding buildingMiner = (AbstractBuilding) citizen.getWorkBuilding();
                if (buildingMiner instanceof BuildingMiner && citizen.getColony() != null && citizen.getColony().getWorld() != null && citizen.getJob() instanceof JobMiner)
                {
                    return getLastLadder(((BuildingMiner) buildingMiner).getLadderLocation(), citizen.getColony().getWorld()) < ((BuildingMiner) buildingMiner).getDepthLimit(
                        citizen.getColony().getWorld())
                        && buildingMiner.getModule(BuildingModules.MINER_LEVELS).getNumberOfLevels() == 0;
                }
                return false;
            });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(WORKER_AI_EXCEPTION),
            citizen -> citizen.getJob() != null && ((AbstractEntityAIBasic<?, ?>) citizen.getJob().getWorkerAI()).getExceptionTimer() > 1);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + HOMELESSNESS),
            citizen -> citizen.getHomeBuilding() == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + UNEMPLOYMENT),
            citizen -> citizen.getJob() == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + IDLEATJOB),
            citizen -> citizen.getJob() != null && citizen.isIdleAtJob());


        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COREMOD_BEEKEEPER_NOFLOWERS),
            citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper
                && ((BuildingBeekeeper) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST)).getList().isEmpty());


        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COREMOD_ENTITY_CITIZEN_SLEEPING),
            citizen -> citizen.getEntity().isPresent() && citizen.isAsleep());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COREMOD_ENTITY_CITIZEN_MOURNING),
            citizen -> citizen.getEntity().isPresent() && citizen.getCitizenMournHandler().isMourning());


        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(QUARRY_MINER_NO_QUARRY),
            citizen -> citizen.getJob() instanceof JobQuarrier && ((JobQuarrier) citizen.getJob()).findQuarry() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(QUARRY_MINER_FINISHED_QUARRY),
            citizen -> citizen.getJob() instanceof JobQuarrier && ((JobQuarrier) citizen.getJob()).findQuarry() != null && ((JobQuarrier) citizen.getJob()).findQuarry()
                .getFirstModuleOccurance(QuarryModule.class)
                .isFinished());
    }
}
