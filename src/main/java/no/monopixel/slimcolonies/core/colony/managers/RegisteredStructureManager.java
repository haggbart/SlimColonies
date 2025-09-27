package no.monopixel.slimcolonies.core.colony.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import no.monopixel.slimcolonies.api.IMinecoloniesAPI;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.colony.buildings.*;
import no.monopixel.slimcolonies.api.colony.buildings.registry.IBuildingDataManager;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.ITownHall;
import no.monopixel.slimcolonies.api.colony.buildings.workerbuildings.IWareHouse;
import no.monopixel.slimcolonies.api.colony.managers.interfaces.IRegisteredStructureManager;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.eventbus.events.colony.buildings.BuildingAddedModEvent;
import no.monopixel.slimcolonies.api.eventbus.events.colony.buildings.BuildingRemovedModEvent;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.util.*;
import no.monopixel.slimcolonies.core.MineColonies;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.blocks.huts.BlockHutTavern;
import no.monopixel.slimcolonies.core.blocks.huts.BlockHutTownHall;
import no.monopixel.slimcolonies.core.colony.Colony;
import no.monopixel.slimcolonies.core.colony.buildingextensions.registry.BuildingExtensionDataManager;
import no.monopixel.slimcolonies.core.colony.buildings.BuildingMysticalSite;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.BuildingModules;
import no.monopixel.slimcolonies.core.colony.buildings.modules.LivingBuildingModule;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.*;
import no.monopixel.slimcolonies.core.entity.ai.workers.util.ConstructionTapeHelper;
import no.monopixel.slimcolonies.core.event.QuestObjectiveEventHandler;
import no.monopixel.slimcolonies.core.network.messages.client.colony.ColonyViewBuildingExtensionsUpdateMessage;
import no.monopixel.slimcolonies.core.network.messages.client.colony.ColonyViewBuildingViewMessage;
import no.monopixel.slimcolonies.core.network.messages.client.colony.ColonyViewRemoveBuildingMessage;
import no.monopixel.slimcolonies.core.tileentities.TileEntityDecorationController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.MathUtils.RANDOM;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.WARNING_DUPLICATE_TAVERN;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.WARNING_DUPLICATE_TOWN_HALL;

public class RegisteredStructureManager implements IRegisteredStructureManager
{
    /**
     * List of building in the colony.
     */
    @NotNull
    private ImmutableMap<BlockPos, IBuilding> buildings = ImmutableMap.of();

    /**
     * List of building extensions of the colony.
     */
    private final Map<IBuildingExtension.ExtensionId, IBuildingExtension> buildingExtensions = new HashMap<>();

    /**
     * The warehouse building position. Initially null.
     */
    private final List<IWareHouse> wareHouses = new ArrayList<>();

    /**
     * The warehouse building position. Initially null.
     */
    private final List<IMysticalSite> mysticalSites = new ArrayList<>();


    /**
     * The townhall of the colony.
     */
    @Nullable
    private ITownHall townHall;

    /**
     * Variable to check if the buildings needs to be synced.
     */
    private boolean isBuildingsDirty = false;

    /**
     * Variable to check if the building extensions needs to be synced.
     */
    private boolean isBuildingExtensionsDirty = false;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Max chunk pos where a building is placed into a certain direction.
     */
    private int minChunkX;
    private int maxChunkX;
    private int minChunkZ;
    private int maxChunkZ;

    /**
     * Creates the BuildingManager for a colony.
     *
     * @param colony the colony.
     */
    public RegisteredStructureManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        buildings = ImmutableMap.of();
        maxChunkX = colony.getCenter().getX() >> 4;
        minChunkX = colony.getCenter().getX() >> 4;
        maxChunkZ = colony.getCenter().getZ() >> 4;
        minChunkZ = colony.getCenter().getZ() >> 4;

        // Building extensions (previously fields)
        final ListTag extensionsTagList;
        if (compound.contains(TAG_FIELDS))
        {
            extensionsTagList = compound.getList(TAG_FIELDS, Tag.TAG_COMPOUND);
        }
        else
        {
            extensionsTagList = compound.getList(TAG_BUILDING_EXTENSIONS, Tag.TAG_COMPOUND);
        }
        for (int i = 0; i < extensionsTagList.size(); ++i)
        {
            final CompoundTag extensionCompound = extensionsTagList.getCompound(i);
            final IBuildingExtension extension = BuildingExtensionDataManager.compoundToExtension(extensionCompound);
            if (extension != null)
            {
                addBuildingExtension(extension);
            }
        }

        //  Buildings
        final ListTag buildingTagList = compound.getList(TAG_BUILDINGS, Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.size(); ++i)
        {
            final CompoundTag buildingCompound = buildingTagList.getCompound(i);
            @Nullable final IBuilding b = IBuildingDataManager.getInstance().createFrom(colony, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
                setMaxChunk(b);
            }
        }


        // Ensure building extensions are still tied to an appropriate building
        for (final IBuildingExtension extension : buildingExtensions.values())
        {
            if (!extension.isTaken())
            {
                continue;
            }
            final IBuilding building = buildings.get(extension.getBuildingId());
            if (building == null)
            {
                extension.resetOwningBuilding();
                continue;
            }

            final BuildingExtensionsModule extensionsModule = building.getFirstModuleOccurance(BuildingExtensionsModule.class);
            if (extensionsModule == null || !extension.getClass().equals(extensionsModule.getExpectedExtensionType()))
            {
                extension.resetOwningBuilding();
                if (extensionsModule != null)
                {
                    extensionsModule.freeExtension(extension);
                }
            }
        }
    }

    /**
     * Set the max chunk direction this building is in.
     *
     * @param b the max chunk dir.
     */
    private void setMaxChunk(final IBuilding b)
    {
        final int chunkX = b.getPosition().getX() >> 4;
        final int chunkZ = b.getPosition().getZ() >> 4;
        if (chunkX >= maxChunkX)
        {
            maxChunkX = chunkX + 1;
        }

        if (chunkX <= minChunkX)
        {
            minChunkX = chunkX - 1;
        }

        if (chunkZ >= maxChunkZ)
        {
            maxChunkZ = chunkZ + 1;
        }

        if (chunkZ <= minChunkZ)
        {
            minChunkZ = chunkZ - 1;
        }
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        //  Buildings
        @NotNull final ListTag buildingTagList = new ListTag();
        for (@NotNull final IBuilding b : buildings.values())
        {
            @NotNull final CompoundTag buildingCompound = b.serializeNBT();
            buildingTagList.add(buildingCompound);
        }
        compound.put(TAG_BUILDINGS, buildingTagList);

        // Building extensions
        compound.put(TAG_BUILDING_EXTENSIONS, buildingExtensions.values().stream().map(BuildingExtensionDataManager::extensionToCompound).collect(NBTUtils.toListNBT()));

    }

    @Override
    public void clearDirty()
    {
        isBuildingsDirty = false;
        isBuildingExtensionsDirty = false;
        buildings.values().forEach(IBuilding::clearDirty);
    }

    @Override
    public void sendPackets(final Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers)
    {
        sendBuildingPackets(closeSubscribers, newSubscribers);
        sendBuildingExtensionPackets(closeSubscribers, newSubscribers);
        isBuildingsDirty = false;
        isBuildingExtensionsDirty = false;
    }

    @Override
    public void onColonyTick(final IColony colony)
    {
        //  Tick Buildings
        for (@NotNull final IBuilding building : buildings.values())
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
            {
                building.onColonyTick(colony);
            }
        }
    }

    @Override
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public void cleanUpBuildings(@NotNull final IColony colony)
    {
        @Nullable final List<IBuilding> removedBuildings = new ArrayList<>();

        //Need this list, we may enter here while we add a building in the real world.
        final List<IBuilding> tempBuildings = new ArrayList<>(buildings.values());

        for (@NotNull final IBuilding building : tempBuildings)
        {
            final BlockPos loc = building.getPosition();
            if (WorldUtil.isBlockLoaded(colony.getWorld(), loc) && !building.isMatchingBlock(colony.getWorld().getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                removedBuildings.add(building);
            }
        }

        if (buildingExtensions.entrySet().removeIf(extension -> WorldUtil.isBlockLoaded(colony.getWorld(), extension.getValue().getPosition())
            && (!colony.isCoordInColony(colony.getWorld(), extension.getValue().getPosition()) || !extension.getValue().isValidPlacement(colony))))
        {
            markBuildingExtensionsDirty();
        }


        if (!removedBuildings.isEmpty() && removedBuildings.size() >= buildings.values().size())
        {
            Log.getLogger()
                .warn("Colony:" + colony.getID()
                    + " is removing all buildings at once. Did you just load a backup? If not there is a chance that colony data got corrupted and you want to restore a backup.");
        }

        removedBuildings.forEach(IBuilding::destroy);
    }

    @Override
    public IBuilding getBuilding(final BlockPos buildingId)
    {
        if (buildingId != null)
        {
            return buildings.get(buildingId);
        }
        return null;
    }



    @Nullable
    @Override
    public IBuilding getFirstBuildingMatching(final Predicate<IBuilding> predicate)
    {
        for (final IBuilding building : buildings.values())
        {
            if (predicate.test(building))
            {
                return building;
            }
        }
        return null;
    }


    @Nullable
    @Override
    public IWareHouse getClosestWarehouseInColony(final BlockPos pos)
    {
        IWareHouse wareHouse = null;
        double dist = 0;
        for (final IWareHouse building : wareHouses)
        {
            if (building.getBuildingLevel() > 0 && building.getTileEntity() != null)
            {
                final double tempDist = building.getPosition().distSqr(pos);
                if (wareHouse == null || tempDist < dist)
                {
                    dist = tempDist;
                    wareHouse = building;
                }
            }
        }

        return wareHouse;
    }

    @Override
    public boolean keepChunkColonyLoaded(final LevelChunk chunk)
    {
        final Set<BlockPos> capList = ColonyUtils.getAllClaimingBuildings(chunk).get(colony.getID());
        return capList != null && capList.size() >= MineColonies.getConfig().getServer().colonyLoadStrictness.get();
    }

    @Override
    public IBuilding getHouseWithSpareBed()
    {
        for (final IBuilding building : buildings.values())
        {
            if (building.hasModule(LivingBuildingModule.class))
            {
                final LivingBuildingModule module = building.getFirstModuleOccurance(LivingBuildingModule.class);
                if (HiringMode.LOCKED.equals(module.getHiringMode()))
                {
                    continue;
                }
                if (module.getAssignedCitizen().size() < module.getModuleMax())
                {
                    return building;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Map<BlockPos, IBuilding> getBuildings()
    {
        return buildings;
    }

    @Nullable
    @Override
    public ITownHall getTownHall()
    {
        return townHall;
    }

    @Override
    public int getMysticalSiteMaxBuildingLevel()
    {
        int maxLevel = 0;
        if (hasMysticalSite())
        {
            for (final IMysticalSite mysticalSite : mysticalSites)
            {
                if (mysticalSite.getBuildingLevel() > maxLevel)
                {
                    maxLevel = mysticalSite.getBuildingLevel();
                }
            }
        }
        return maxLevel;
    }

    @Override
    public boolean hasWarehouse()
    {
        return !wareHouses.isEmpty();
    }

    @Override
    public boolean hasMysticalSite()
    {
        return !mysticalSites.isEmpty();
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    @Override
    public <B extends IBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type)
    {
        try
        {
            return type.cast(buildings.get(buildingId));
        }
        catch (final ClassCastException e)
        {
            Log.getLogger().warn("getBuilding called with wrong type: ", e);
            return null;
        }
    }

    @Override
    public IBuilding addNewBuilding(@NotNull final AbstractTileEntityColonyBuilding tileEntity, final Level world)
    {
        tileEntity.setColony(colony);
        if (!buildings.containsKey(tileEntity.getPosition()))
        {
            @Nullable final IBuilding building = IBuildingDataManager.getInstance().createFrom(colony, tileEntity);
            if (building != null)
            {
                addBuilding(building);
                tileEntity.setBuilding(building);
                building.upgradeBuildingLevelToSchematicData();

                Log.getLogger().debug(String.format("Colony %d - new Building %s for %s at %s",
                    colony.getID(),
                    building.getBuildingDisplayName(),
                    tileEntity.getBlockState().getBlock(),
                    tileEntity.getPosition()));

                building.setIsMirrored(tileEntity.isMirrored());
                if (tileEntity.getBlockState().getBlock() instanceof AbstractBlockHut<?>)
                {
                    if (tileEntity.getStructurePack() != null)
                    {
                        building.setStructurePack(tileEntity.getStructurePack().getName());
                        building.setBlueprintPath(tileEntity.getBlueprintPath());
                    }
                    else
                    {
                        building.setStructurePack(colony.getStructurePack());
                    }
                }

                if (world != null && !(building instanceof IRSComponent))
                {
                    building.onPlacement();
                    ConstructionTapeHelper.placeConstructionTape(building);
                }

                colony.getRequestManager().onProviderAddedToColony(building);

                setMaxChunk(building);
            }
            else
            {
                Log.getLogger().error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
                    colony.getID(),
                    tileEntity.getBlockState().getClass(),
                    tileEntity.getPosition()), new Exception());
            }

            colony.getCitizenManager().calculateMaxCitizens();
            colony.getPackageManager().updateSubscribers();

            IMinecoloniesAPI.getInstance().getEventBus().post(new BuildingAddedModEvent(building));

            return building;
        }
        return null;
    }

    @Override
    public void removeBuilding(@NotNull final IBuilding building, final Set<ServerPlayer> subscribers)
    {
        if (buildings.containsKey(building.getID()))
        {
            final ImmutableMap.Builder<BlockPos, IBuilding> builder = new ImmutableMap.Builder<>();
            for (final IBuilding tbuilding : buildings.values())
            {
                if (tbuilding != building)
                {
                    builder.put(tbuilding.getID(), tbuilding);
                }
            }

            buildings = builder.build();

            for (final ServerPlayer player : subscribers)
            {
                Network.getNetwork().sendToPlayer(new ColonyViewRemoveBuildingMessage(colony, building.getID()), player);
            }

            Log.getLogger().info(String.format("Colony %d - removed AbstractBuilding %s of type %s",
                colony.getID(),
                building.getID(),
                building.getSchematicName()));
        }

        if (building instanceof BuildingTownHall)
        {
            townHall = null;
        }
        else if (building instanceof BuildingWareHouse)
        {
            wareHouses.remove(building);
        }
        else if (building instanceof BuildingMysticalSite)
        {
            mysticalSites.remove(building);
        }

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.onRemoveBuilding(building);
            building.cancelAllRequestsOfCitizenOrBuilding(citizen);
        }

        colony.getRequestManager().onProviderRemovedFromColony(building);
        colony.getRequestManager().onRequesterRemovedFromColony(building.getRequester());

        colony.getCitizenManager().calculateMaxCitizens();

        IMinecoloniesAPI.getInstance().getEventBus().post(new BuildingRemovedModEvent(building));
    }

    @Override
    public BlockPos getBestBuilding(final AbstractEntityCitizen citizen, final Class<? extends IBuilding> building)
    {
        return getBestBuilding(citizen.blockPosition(), building);
    }

    @Override
    public <T extends IBuilding> BlockPos getBestBuilding(final AbstractEntityCitizen citizen, final Class<T> building, @NotNull final Predicate<T> filter)
    {
        return getBestBuilding(citizen.blockPosition(), building, filter);
    }

    @Override
    public BlockPos getBestBuilding(final BlockPos pos, final Class<? extends IBuilding> building)
    {
        return getBestBuilding(pos, building, b -> true);
    }

    @Override
    public <T extends IBuilding> BlockPos getBestBuilding(final BlockPos pos, final Class<T> building, @NotNull final Predicate<T> filter)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodCook = null;
        for (final IBuilding currentBuilding : buildings.values())
        {
            if (building.isInstance(currentBuilding) && currentBuilding.getBuildingLevel() > 0 && WorldUtil.isBlockLoaded(colony.getWorld(), currentBuilding.getPosition())
                && filter.test(
                (T) currentBuilding))
            {
                final double localDistance = currentBuilding.getPosition().distSqr(pos);
                if (localDistance < distance)
                {
                    distance = localDistance;
                    goodCook = currentBuilding.getPosition();
                }
            }
        }
        return goodCook;
    }

    @Override
    public BlockPos getRandomBuilding(Predicate<IBuilding> filterPredicate)
    {
        final List<IBuilding> allowedBuildings = new ArrayList<>();
        for (final IBuilding building : buildings.values())
        {
            if (filterPredicate.test(building))
            {
                allowedBuildings.add(building);
            }
        }

        if (allowedBuildings.isEmpty())
        {
            return null;
        }

        return allowedBuildings.get(RANDOM.nextInt(allowedBuildings.size())).getPosition();
    }

    /**
     * Finds whether there is a guard building close to the given building
     *
     * @param building the building to check.
     * @return false if no guard tower close, true in other cases
     */
    @Override
    public boolean hasGuardBuildingNear(final IBuilding building)
    {
        if (building == null)
        {
            return true;
        }

        for (final IBuilding colonyBuilding : getBuildings().values())
        {
            if (colonyBuilding.getBuildingLevel() > 0 && (colonyBuilding instanceof IGuardBuilding || colonyBuilding instanceof BuildingBarracks))
            {
                final BoundingBox guardedRegion = BlockPosUtil.getChunkAlignedBB(colonyBuilding.getPosition(), colonyBuilding.getClaimRadius(colonyBuilding.getBuildingLevel()));
                if (guardedRegion.isInside(building.getPosition()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void setTownHall(@Nullable final ITownHall building)
    {
        this.townHall = building;
    }

    @Override
    public List<IWareHouse> getWareHouses()
    {
        return wareHouses;
    }

    @Override
    public void removeWareHouse(final IWareHouse wareHouse)
    {
        wareHouses.remove(wareHouse);
    }

    @Override
    public List<IMysticalSite> getMysticalSites()
    {
        return mysticalSites;
    }

    @Override
    public void removeMysticalSite(final IMysticalSite mysticalSite)
    {
        mysticalSites.remove(mysticalSite);
    }

    @Override
    public void markBuildingExtensionsDirty()
    {
        isBuildingExtensionsDirty = true;
    }

    /**
     * Add a AbstractBuilding to the Colony.
     *
     * @param building AbstractBuilding to add to the colony.
     */
    private void addBuilding(@NotNull final IBuilding building)
    {
        buildings = new ImmutableMap.Builder<BlockPos, IBuilding>().putAll(buildings).put(building.getID(), building).build();

        building.markDirty();

        //  Limit 1 town hall
        if (building instanceof BuildingTownHall && townHall == null)
        {
            townHall = (ITownHall) building;
        }

        if (building instanceof BuildingWareHouse)
        {
            wareHouses.add((IWareHouse) building);
        }
        else if (building instanceof BuildingMysticalSite)
        {
            mysticalSites.add((IMysticalSite) building);
        }
    }

    /**
     * Sends packages to update the buildings.
     *
     * @param closeSubscribers the current event subscribers.
     * @param newSubscribers   the new event subscribers.
     */
    private void sendBuildingPackets(final Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers)
    {
        if (isBuildingsDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();
            if (isBuildingsDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            for (@NotNull final IBuilding building : buildings.values())
            {
                if (building.isDirty() || !newSubscribers.isEmpty())
                {
                    final ColonyViewBuildingViewMessage message = new ColonyViewBuildingViewMessage(building, !newSubscribers.isEmpty());
                    players.forEach(player -> Network.getNetwork().sendToPlayer(message, player));
                }
            }
        }
    }

    /**
     * Sends packages to update the building extensions.
     *
     * @param closeSubscribers the current event subscribers.
     * @param newSubscribers   the new event subscribers.
     */
    private void sendBuildingExtensionPackets(final Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers)
    {
        if (isBuildingExtensionsDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();
            if (isBuildingExtensionsDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            players.forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewBuildingExtensionsUpdateMessage(colony, buildingExtensions.values()), player));
        }
    }

    @Override
    public boolean canPlaceAt(final Block block, final BlockPos pos, final Player player)
    {
        if (block instanceof BlockHutTownHall)
        {
            if (colony.hasTownHall())
            {
                if (colony.getWorld() != null && !colony.getWorld().isClientSide)
                {
                    MessageUtils.format(WARNING_DUPLICATE_TOWN_HALL, townHall.getPosition().toShortString()).sendTo(player);
                }
                return false;
            }
            return true;
        }
        else if (block instanceof BlockHutTavern)
        {
            for (final IBuilding building : buildings.values())
            {
                if (building.hasModule(BuildingModules.TAVERN_VISITOR))
                {
                    MessageUtils.format(WARNING_DUPLICATE_TAVERN, building.getPosition().toShortString()).sendTo(player);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onBuildingUpgradeComplete(@Nullable final IBuilding building, final int level)
    {
        if (building != null)
        {
            colony.getCitizenManager().calculateMaxCitizens();
            markBuildingsDirty();
            QuestObjectiveEventHandler.onBuildingUpgradeComplete(building, level);
        }
    }

    @NotNull
    @Override
    public List<IBuildingExtension> getBuildingExtensions(Predicate<IBuildingExtension> matcher)
    {
        return buildingExtensions.values().stream()
            .filter(matcher)
            .toList();
    }

    @Override
    public Optional<IBuildingExtension> getMatchingBuildingExtension(Predicate<IBuildingExtension> matcher)
    {
        return getBuildingExtensions(matcher)
            .stream()
            .findFirst();
    }

    @Override
    public boolean addBuildingExtension(IBuildingExtension extension)
    {
        if (buildingExtensions.putIfAbsent(extension.getId(), extension) == null)
        {
            markBuildingExtensionsDirty();
            return true;
        }
        return false;
    }

    @Override
    public void removeBuildingExtension(Predicate<IBuildingExtension> matcher)
    {
        buildingExtensions.entrySet().removeIf(entry -> matcher.test(entry.getValue()));

        // We must send the message to everyone since building extensions here will be permanently removed from the list.
        // And the clients have no way to later on also get their building extensions removed, thus every client has to be told
        // immediately that the building extension is gone.
        markBuildingExtensionsDirty();
    }

    @Override
    @Nullable
    public IBuildingExtension getMatchingBuildingExtension(final IBuildingExtension.ExtensionId extensionId)
    {
        return buildingExtensions.get(extensionId);
    }
}
