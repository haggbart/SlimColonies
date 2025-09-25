package no.monopixel.slimcolonies.api.tileentities;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.storage.StructurePackMeta;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingContainer;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.util.InventoryFunctions;
import no.monopixel.slimcolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractTileEntityColonyBuilding extends TileEntityRack implements IBlueprintDataProviderBE
{
    /**
     * Version of the TE data.
     */
    private static final String TAG_VERSION = "version";
    private static final int    VERSION     = 2;

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1             = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    /**
     * The TE's schematic name
     */
    private String schematicName = "";

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    /**
     * Check if the building might have old data.
     */
    private int                        version = 0;

    /**
     * Tag map cache.
     */
    private Map<String, Set<BlockPos>> worldTagMapCache = null;

    /**
     * List based tag map cache.
     */
    private Map<String, List<BlockPos>> worldTagMapCacheWithList;

    public AbstractTileEntityColonyBuilding(final BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}. It will be taken from the chest and placed in the worker inventory. Make sure that the worker stands next the chest to
     * not break immersion. Also make sure to have inventory space for the stack.
     *
     * @param entity                      the tileEntity chest or building.
     * @param itemStackSelectionPredicate the itemStack predicate.
     * @return true if found the stack.
     */
    public static boolean isInTileEntity(final ICapabilityProvider entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return InventoryFunctions.matchFirstInProvider(entity, itemStackSelectionPredicate);
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    public abstract int getColonyId();

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    public abstract IColony getColony();

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    public abstract void setColony(IColony c);

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    public abstract BlockPos getPosition();

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public abstract BlockPos getPositionOfChestWithItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuildingContainer} associated with the tile entity.
     */
    public abstract IBuilding getBuilding();

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link IBuildingContainer} to associate with the tile entity.
     */
    public abstract void setBuilding(IBuilding b);

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link IBuildingView} the tile entity is associated with.
     */
    public abstract IBuildingView getBuildingView();

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    public abstract boolean hasAccessPermission(Player player);

    /**
     * Set if the entity is mirrored.
     *
     * @param mirror true if so.
     */
    public abstract void setMirror(boolean mirror);

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    public abstract boolean isMirrored();

    /**
     * Getter for the style.
     *
     * @return the pack of it.
     */
    public abstract StructurePackMeta getStructurePack();

    /**
     * Set the pack of the tileEntity.
     *
     * @param style the pack to set.
     */
    public abstract void setStructurePack(final StructurePackMeta style);

    /**
     * Set the blueprint path of the tileEntity.
     *
     * @param path the path to set.
     */
    public abstract void setBlueprintPath(final String path);

    /**
     * Get the blueprint path of the tileEntity.
     *
     * @return  path the path to get.
     */
    public abstract String getBlueprintPath();

    /**
     * Get the building name that this {@link AbstractTileEntityColonyBuilding} belongs to.
     *
     * @return The buildings name.
     */
    public abstract ResourceLocation getBuildingName();

    @Override
    public String getSchematicName()
    {
        return schematicName.replace(".blueprint", "");
    }

    @Override
    public void setSchematicName(final String name)
    {
        schematicName = name;
    }

    @Override
    public Map<BlockPos, List<String>> getPositionedTags()
    {
        return tagPosMap;
    }

    @Override
    public Map<String, Set<BlockPos>> getWorldTagNamePosMap()
    {
        if (worldTagMapCache == null)
        {
            worldTagMapCache = IBlueprintDataProviderBE.super.getWorldTagNamePosMap();
        }
        return worldTagMapCache;
    }

    /**
     * Get a list version of the positioned tags, mapped from tag name to position.
     * @return the list version.
     */
    public Map<String, List<BlockPos>> getCachedWorldTagNamePosMap()
    {
        if (worldTagMapCacheWithList == null)
        {
            final Map<String, Set<BlockPos>> worldTagNamePosMap = getWorldTagNamePosMap();
            worldTagMapCacheWithList = new HashMap<>();
            for (final Map.Entry<String, Set<BlockPos>> entry : worldTagNamePosMap.entrySet())
            {
                worldTagMapCacheWithList.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return worldTagMapCacheWithList;
    }

    @Override
    public void setPositionedTags(final Map<BlockPos, List<String>> positionedTags)
    {
        tagPosMap = positionedTags;
        worldTagMapCache = null;
        worldTagMapCacheWithList = null;
        setChanged();
    }

    @Override
    public Tuple<BlockPos, BlockPos> getSchematicCorners()
    {
        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setSchematicCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
        setChanged();
    }

    @Override
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);
        readSchematicDataFromNBT(compound);
        this.version = compound.getInt(TAG_VERSION);
    }

    @Override
    public void readSchematicDataFromNBT(final CompoundTag originalCompound)
    {
        final String old = getSchematicName();
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(originalCompound);

        if (level == null || level.isClientSide || getColony() == null || getColony().getBuildingManager() == null)
        {
            return;
        }

        final IBuilding building = getColony().getBuildingManager().getBuilding(worldPosition);
        if (building != null)
        {
            building.onUpgradeSchematicTo(old, getSchematicName(), this);
        }
        this.version = VERSION;
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
        compound.putInt(TAG_VERSION, this.version);
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    /**
     * Check if the TE is on an old data version.
     * @return true if so.
     */
    public boolean isOutdated()
    {
        return version < VERSION;
    }
}
