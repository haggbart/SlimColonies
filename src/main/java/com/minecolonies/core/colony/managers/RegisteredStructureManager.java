package com.minecolonies.core.colony.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.*;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.managers.interfaces.IRegisteredStructureManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.blocks.huts.BlockHutTavern;
import com.minecolonies.core.blocks.huts.BlockHutTownHall;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.BuildingMysticalSite;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.BuildingExtensionsModule;
import com.minecolonies.core.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBarracks;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.buildingextensions.registry.BuildingExtensionDataManager;
import com.minecolonies.core.entity.ai.workers.util.ConstructionTapeHelper;
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import com.minecolonies.core.network.messages.client.colony.ColonyViewBuildingViewMessage;
import com.minecolonies.core.network.messages.client.colony.ColonyViewBuildingExtensionsUpdateMessage;
import com.minecolonies.core.network.messages.client.colony.ColonyViewRemoveBuildingMessage;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.MathUtils.RANDOM;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_DUPLICATE_TAVERN;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_DUPLICATE_TOWN_HALL;

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
     * List of leisure sites.
     */
    private ImmutableList<BlockPos> leisureSites = ImmutableList.of();

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

        if (compound.contains(TAG_LEISURE))
        {
            final ListTag leisureTagList = compound.getList(TAG_LEISURE, Tag.TAG_COMPOUND);
            final List<BlockPos> leisureSitesList = new ArrayList<>();
            for (int i = 0; i < leisureTagList.size(); ++i)
            {
                final BlockPos pos = BlockPosUtil.read(leisureTagList.getCompound(i), TAG_POS);
                if (!leisureSitesList.contains(pos))
                {
                    leisureSitesList.add(pos);
                }
            }
            leisureSites = ImmutableList.copyOf(leisureSitesList);
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

        // Leisure sites
        @NotNull final ListTag leisureTagList = new ListTag();
        for (@NotNull final BlockPos pos : leisureSites)
        {
            @NotNull final CompoundTag leisureCompound = new CompoundTag();
            BlockPosUtil.write(leisureCompound, TAG_POS, pos);
            leisureTagList.add(leisureCompound);
        }
        compound.put(TAG_LEISURE, leisureTagList);
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

        for (@NotNull final BlockPos pos : leisureSites)
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), pos) && (!(colony.getWorld().getBlockEntity(pos) instanceof TileEntityDecorationController)))
            {
                removeLeisureSite(pos);
            }
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

    @Override
    public List<BlockPos> getLeisureSites()
    {
        return leisureSites;
    }

    @Override
    public BlockPos getRandomLeisureSite()
    {
        final boolean isRaining = colony.getWorld().isRaining();

        IBuilding building = null;
        final int randomDist = RANDOM.nextInt(4);
        if (randomDist < 1)
        {
            building = getFirstBuildingMatching(b -> b instanceof BuildingTownHall && b.getBuildingLevel() >= 3);
            if (building != null)
            {
                return building.getPosition();
            }
        }

        if (randomDist < 2)
        {
            if (!isRaining && RANDOM.nextBoolean())
            {
                building = getFirstBuildingMatching(b -> b instanceof BuildingMysticalSite && b.getBuildingLevel() >= 1);
                if (building != null)
                {
                    return building.getPosition();
                }
            }
            else
            {
                building = getFirstBuildingMatching(b -> b instanceof BuildingLibrary && b.getBuildingLevel() >= 1);
                if (building != null)
                {
                    return building.getPosition();
                }
            }
        }

        if (randomDist < 3)
        {
            building = getFirstBuildingMatching(b -> b.hasModule(BuildingModules.TAVERN_VISITOR) && b.getBuildingLevel() >= 1);
            if (building != null)
            {
                return building.getPosition();
            }
        }

        if (isRaining)
        {
            return null;
        }

        return leisureSites.isEmpty() ? null : leisureSites.get(RANDOM.nextInt(leisureSites.size()));
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

    @Override
    public void addLeisureSite(final BlockPos pos)
    {
        final List<BlockPos> tempList = new ArrayList<>(leisureSites);
        if (!tempList.contains(pos))
        {
            tempList.add(pos);
            this.leisureSites = ImmutableList.copyOf(tempList);
            markBuildingsDirty();
        }
    }

    @Override
    public void removeLeisureSite(final BlockPos pos)
    {
        if (leisureSites.contains(pos))
        {
            final List<BlockPos> tempList = new ArrayList<>(leisureSites);
            tempList.remove(pos);
            this.leisureSites = ImmutableList.copyOf(tempList);
            markBuildingsDirty();
        }
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
            building.cancelAllRequestsOfCitizen(citizen);
        }

        colony.getRequestManager().onProviderRemovedFromColony(building);
        colony.getRequestManager().onRequesterRemovedFromColony(building.getRequester());

        colony.getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public BlockPos getBestBuilding(final AbstractEntityCitizen citizen, final Class<? extends IBuilding> clazz)
    {
        return getBestBuilding(citizen.blockPosition(), clazz);
    }

    @Override
    public BlockPos getBestBuilding(final BlockPos citizen, final Class<? extends IBuilding> clazz)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodCook = null;
        for (final IBuilding building : buildings.values())
        {
            if (clazz.isInstance(building) && building.getBuildingLevel() > 0 && WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
            {
                final double localDistance = building.getPosition().distSqr(citizen);
                if (localDistance < distance)
                {
                    distance = localDistance;
                    goodCook = building.getPosition();
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
    public void guardBuildingChangedAt(final IBuilding guardBuilding, final int newLevel)
    {
        final int claimRadius = guardBuilding.getClaimRadius(Math.max(guardBuilding.getBuildingLevel(), newLevel));
        final BoundingBox guardedRegion = BlockPosUtil.getChunkAlignedBB(guardBuilding.getPosition(), claimRadius);
        for (final IBuilding building : getBuildings().values())
        {
            if (guardedRegion.isInside(building.getPosition()))
            {
                building.resetGuardBuildingNear();
            }
        }
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
