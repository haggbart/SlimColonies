package no.monopixel.slimcolonies.api.util;

import no.monopixel.slimcolonies.api.colony.IColonyTagCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.monopixel.slimcolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

/**
 * The chunkload storage used to load chunks with colony information.
 */
public class ChunkLoadStorage
{
    /**
     * NBT tag for claims to add
     */
    public static final String TAG_CLAIM_LIST = "claimsToAdd";

    /**
     * The max amount of claim caches we stack
     */
    private static final int MAX_CHUNK_CLAIMS = 20;

    /**
     * The colony id.
     */
    private final List<Short> owningChanges = new ArrayList<>();

    /**
     * XZ pos as long.
     */
    private final long xz;

    /**
     * The dimension of the chunk.
     */
    private final ResourceLocation dimension;

    /**
     * The building claiming this.
     */
    private final List<Tuple<Short, BlockPos>> claimingBuilding = new ArrayList<>();

    /**
     * The building unclaiming this.
     */
    private final List<Tuple<Short, BlockPos>> unClaimingBuilding = new ArrayList<>();

    /**
     * Intitialize a ChunLoadStorage from nbt.
     *
     * @param compound the compound to use.
     */
    public ChunkLoadStorage(final CompoundTag compound)
    {
        if (compound.contains(TAG_ID))
        {
            this.owningChanges.add(compound.getShort(TAG_ID));
        }

        this.xz = compound.getLong(TAG_POS);
        this.dimension = new ResourceLocation(compound.getString(TAG_DIMENSION));

        owningChanges.addAll(NBTUtils.streamCompound(compound.getList(TAG_CLAIM_LIST, Tag.TAG_COMPOUND))
          .map(tempCompound -> tempCompound.getShort(TAG_COLONY_ID)).collect(Collectors.toList()));

        claimingBuilding.addAll(NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_CLAIM, Tag.TAG_COMPOUND))
                                  .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
        unClaimingBuilding.addAll(NBTUtils.streamCompound(compound.getList(TAG_BUILDINGS_UNCLAIM, Tag.TAG_COMPOUND))
                                    .map(ChunkLoadStorage::readTupleFromNbt).collect(Collectors.toList()));
    }

    /**
     * Create a new chunkload storage.
     *
     * @param colonyId  the id of the colony.
     * @param xz        the chunk xz.
     * @param dimension the dimension.
     * @param building  the building claiming this chunk.
     */
    public ChunkLoadStorage(final int colonyId, final long xz, final ResourceLocation dimension, final BlockPos building, final boolean add)
    {
        this.xz = xz;
        this.dimension = dimension;
        if (add)
        {
            claimingBuilding.add(new Tuple<>((short) colonyId, building));
        }
        else
        {
            unClaimingBuilding.add(new Tuple<>((short) colonyId, building));
        }
    }

    /**
     * Write the ChunkLoadStorage to NBT.
     *
     * @return the compound.
     */
    public CompoundTag toNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putLong(TAG_POS, xz);
        compound.putString(TAG_DIMENSION, dimension.toString());

        compound.put(TAG_CLAIM_LIST, owningChanges.stream().map(ChunkLoadStorage::getCompoundOfColonyId).collect(NBTUtils.toListNBT()));
        compound.put(TAG_BUILDINGS, claimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toListNBT()));
        compound.put(TAG_BUILDINGS, unClaimingBuilding.stream().map(ChunkLoadStorage::writeTupleToNBT).collect(NBTUtils.toListNBT()));

        return compound;
    }

    private static CompoundTag getCompoundOfColonyId(final int id)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_COLONY_ID, id);
        return compound;
    }

    /**
     * Getter for the dimension.
     *
     * @return the dimension id.
     */
    public ResourceLocation getDimension()
    {
        return dimension;
    }

    /**
     * Get the x long.
     *
     * @return the long representing two integers.
     */
    public long getXz()
    {
        return xz;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final ChunkLoadStorage storage = (ChunkLoadStorage) o;
        return xz == storage.xz &&
                 dimension == storage.dimension &&
                 Objects.equals(owningChanges, storage.owningChanges) &&
                 Objects.equals(claimingBuilding, storage.claimingBuilding) &&
                 Objects.equals(unClaimingBuilding, storage.unClaimingBuilding);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(owningChanges, xz, dimension, claimingBuilding, unClaimingBuilding);
    }

    /**
     * Apply this ChunkLoadStorage to a capability.
     *
     * @param chunk the chunk to apply it to.
     * @param cap   the capability to apply it to.
     */
    public void applyToCap(final IColonyTagCapability cap, final LevelChunk chunk)
    {
        // Apply ownership changes
        for (int i = 0; i < owningChanges.size(); i++)
        {
            final int claimID = owningChanges.get(i);
            if (claimID > NO_COLONY_ID)
            {
                cap.setOwningColony(claimID, chunk);
            }
        }

        // Apply building unclaims
        for (final Tuple<Short, BlockPos> tuple : unClaimingBuilding)
        {
            cap.removeBuildingClaim(tuple.getA(), tuple.getB(), chunk);
        }

        // Apply building claims
        for (final Tuple<Short, BlockPos> tuple : claimingBuilding)
        {
            cap.addBuildingClaim(tuple.getA(), tuple.getB(), chunk);
        }

        chunk.setUnsaved(true);
    }

    /**
     * Check if the chunkloadstorage is empty.
     *
     * @return true if so.
     */
    public boolean isEmpty()
    {
        return owningChanges.isEmpty() && claimingBuilding.isEmpty() && unClaimingBuilding.isEmpty();
    }

    /**
     * Merge the two Chunkstorages into one. The newer one is considered to be the "more up to date" version.
     *
     * @param newStorage the new version to add.
     */
    public void merge(final ChunkLoadStorage newStorage)
    {
        // Merge ownership changes
        owningChanges.addAll(newStorage.owningChanges);

        // Clear if too many ownership changes accumulated
        if (owningChanges.size() > MAX_CHUNK_CLAIMS)
        {
            owningChanges.clear();
        }

        // Merge building claims, removing conflicts
        this.claimingBuilding.removeIf(newStorage.unClaimingBuilding::contains);
        this.unClaimingBuilding.removeIf(newStorage.claimingBuilding::contains);

        for (final Tuple<Short, BlockPos> tuple : newStorage.unClaimingBuilding)
        {
            if (!this.unClaimingBuilding.contains(tuple))
            {
                this.unClaimingBuilding.add(tuple);
            }
        }

        for (final Tuple<Short, BlockPos> tuple : newStorage.claimingBuilding)
        {
            if (!this.claimingBuilding.contains(tuple))
            {
                this.claimingBuilding.add(tuple);
            }
        }
    }

    /**
     * Write the tuple to NBT.
     *
     * @param tuple the tuple to write.
     * @return the resulting compound.
     */
    private static CompoundTag writeTupleToNBT(final Tuple<Short, BlockPos> tuple)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putShort(TAG_COLONY_ID, tuple.getA());
        BlockPosUtil.write(compound, TAG_BUILDING, tuple.getB());
        return compound;
    }

    /**
     * Read the tuple from NBT.
     *
     * @param compound the compound to extract it from.
     * @return the tuple.
     */
    private static Tuple<Short, BlockPos> readTupleFromNbt(final CompoundTag compound)
    {
        return new Tuple<>(compound.getShort(TAG_COLONY_ID), BlockPosUtil.read(compound, TAG_BUILDING));
    }
}
