package no.monopixel.slimcolonies.core.colony.buildings.modules;

import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.colony.buildings.modules.AbstractBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IBuildingModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.IPersistentModule;
import no.monopixel.slimcolonies.api.colony.buildings.modules.ITickingModule;
import no.monopixel.slimcolonies.core.SlimColonies;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract class to list all extensions (assigned) to a building.
 */
public abstract class BuildingExtensionsModule extends AbstractBuildingModule implements IPersistentModule, IBuildingModule, ITickingModule
{
    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";
    private static final String TAG_CURRENT_EXTENSION = "currex";

    /**
     * A map of building extensions, along with their world time (ticks) of when they were last reset.
     */
    private final Map<IBuildingExtension.ExtensionId, Long> checkedExtensions = new Object2LongOpenHashMap<>();

    /**
     * The building extension the citizen is currently working on.
     */
    @Nullable
    private IBuildingExtension.ExtensionId currentExtensionId;

    /**
     * Building extensions should be assigned manually to the citizen.
     */
    private boolean shouldAssignManually = false;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        claimExtensions();
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);
        final ListTag listTag = compound.getList(TAG_BUILDING_EXTENSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); ++i)
        {
            final CompoundTag tag = listTag.getCompound(i);
            checkedExtensions.put(IBuildingExtension.ExtensionId.deserializeNBT(tag.getCompound(TAG_ID)), tag.getLong(TAG_TIME));
        }
        if (compound.contains(TAG_CURRENT_EXTENSION))
        {
            currentExtensionId = IBuildingExtension.ExtensionId.deserializeNBT(compound.getCompound(TAG_CURRENT_EXTENSION));
        }

        // Clean up stale entries for extensions that no longer exist
        cleanupStaleEntries();
    }

    /**
     * Removes entries from checkedExtensions for building extensions that no longer exist.
     * Called after deserializing from NBT to clean up deleted fields.
     */
    private void cleanupStaleEntries()
    {
        final List<IBuildingExtension> ownedExtensions = getOwnedExtensions();
        checkedExtensions.keySet().removeIf(id -> ownedExtensions.stream().noneMatch(ext -> ext.getId().equals(id)));
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        final ListTag listTag = new ListTag();
        for (final Map.Entry<IBuildingExtension.ExtensionId, Long> entry : checkedExtensions.entrySet())
        {
            final CompoundTag listEntry = new CompoundTag();
            listEntry.put(TAG_ID, entry.getKey().serializeNBT());
            listEntry.putLong(TAG_TIME, entry.getValue());
            listTag.add(listEntry);
        }
        compound.put(TAG_LIST, listTag);
        if (currentExtensionId != null)
        {
            compound.put(TAG_CURRENT_EXTENSION, currentExtensionId.serializeNBT());
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(shouldAssignManually);
        buf.writeInt(getMaxExtensionCount());

        // Send cooldown data to client
        buf.writeInt(checkedExtensions.size());
        for (final Map.Entry<IBuildingExtension.ExtensionId, Long> entry : checkedExtensions.entrySet())
        {
            buf.writeNbt((CompoundTag) entry.getKey().serializeNBT());
            buf.writeLong(entry.getValue());
        }

        // Send current game time and cooldown config so client can compute cooldown status
        buf.writeLong(building.getColony().getWorld().getGameTime());
        buf.writeInt(SlimColonies.getConfig().getServer().fieldCooldownMinutes.get());
    }

    /**
     * Getter to obtain the maximum building extension count.
     *
     * @return an integer stating the maximum building extension count.
     */
    protected abstract int getMaxExtensionCount();

    /**
     * Get the class type which is expected for the building extension to have.
     *
     * @return the class type.
     */
    public abstract Class<?> getExpectedExtensionType();

    /**
     * Getter of the current building extension.
     *
     * @return a building extension object.
     */
    @Nullable
    public IBuildingExtension getCurrentExtension()
    {
        if (currentExtensionId == null)
        {
            return null;
        }
        return building.getColony().getBuildingManager().getMatchingBuildingExtension(currentExtensionId);
    }

    /**
     * Retrieves the building extension to work on for the citizen, as long as the current building extension has work, it will keep returning that building extension.
     * Else it will retrieve a random building extension to work on for the citizen.
     * This method will also automatically claim any building extensions that are not in use if the building is on automatic assignment mode.
     *
     * @return a building extension to work on, or null if all extensions are on cooldown.
     */
    @Nullable
    public IBuildingExtension getExtensionToWorkOn()
    {
        final IBuildingExtension currentExtension = getCurrentExtension();
        if (currentExtension != null)
        {
            return currentExtension;
        }

        IBuildingExtension.ExtensionId oldestExtension = null;
        long oldestResetTime = Long.MAX_VALUE;

        for (final IBuildingExtension extension : getOwnedExtensions())
        {
            if (!checkedExtensions.containsKey(extension.getId()))
            {
                currentExtensionId = extension.getId();
                return extension;
            }

            final long resetTime = checkedExtensions.get(extension.getId());
            if (resetTime < oldestResetTime)
            {
                oldestExtension = extension.getId();
                oldestResetTime = resetTime;
            }
        }

        // Check if the oldest field is still on cooldown
        if (oldestExtension != null)
        {
            final long currentTime = building.getColony().getWorld().getGameTime();
            final long cooldownTicks = SlimColonies.getConfig().getServer().fieldCooldownMinutes.get() * 60L * 20L;

            if (currentTime - oldestResetTime >= cooldownTicks)
            {
                // Cooldown expired - this field can be worked on
                currentExtensionId = oldestExtension;
                return getCurrentExtension();
            }
        }

        // All fields are on cooldown
        return null;
    }

    /**
     * Returns list of owned building extensions.
     *
     * @return a list of building extension objects.
     */
    @NotNull
    public final List<IBuildingExtension> getOwnedExtensions()
    {
        return getMatchingExtension(f -> building.getID().equals(f.getBuildingId()));
    }

    /**
     * Returns list of building extensions.
     *
     * @return a list of building extension objects.
     */
    @NotNull
    public abstract List<IBuildingExtension> getMatchingExtension(final Predicate<IBuildingExtension>  predicateToMatch);

    /**
     * Attempt to automatically claim free building extensions, if possible and if any building extensions are available.
     */
    public void claimExtensions()
    {
        if (!shouldAssignManually)
        {
            for (final IBuildingExtension extension : getFreeExtensions())
            {
                if (assignExtension(extension))
                {
                    break;
                }
            }
        }
    }

    /**
     * Returns list of free building extensions.
     *
     * @return a list of building extension objects.
     */
    public final List<IBuildingExtension> getFreeExtensions()
    {
        return getMatchingExtension(extension -> !extension.isTaken());
    }

    /**
     * Method called to assign a building extension to the building.
     *
     * @param extension the building extension to add.
     */
    public boolean assignExtension(final IBuildingExtension extension)
    {
        if (canAssignExtension(extension))
        {
            extension.setBuilding(building.getID());
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Check to see if a new building extension can be assigned to the worker.
     *
     * @param extension the building extension which is being added.
     * @return true if so.
     */
    public final boolean canAssignExtension(final IBuildingExtension extension)
    {
        return getOwnedExtensions().size() < getMaxExtensionCount() && canAssignExtensionOverride(extension);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        building.getColony().getBuildingManager().markBuildingExtensionsDirty();
    }

    /**
     * Additional checks to see if this building extension can be assigned to the building.
     *
     * @param extension the building extension which is being added.
     * @return true if so.
     */
    protected abstract boolean canAssignExtensionOverride(IBuildingExtension extension);

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public final boolean assignManually()
    {
        return shouldAssignManually;
    }

    /**
     * Checks if the building has any building extensions.
     *
     * @return true if he has none.
     */
    public final boolean hasNoExtensions()
    {
        return getOwnedExtensions().isEmpty();
    }

    /**
     * Switches the assign manually of the building.
     *
     * @param assignManually true if assignment should be manual.
     */
    public final void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Method called to free a building extension.
     *
     * @param extension the building extension to be freed.
     */
    public void freeExtension(final IBuildingExtension extension)
    {
        extension.resetOwningBuilding();
        markDirty();

        // Remove from checked extensions to prevent memory leak
        checkedExtensions.remove(extension.getId());

        if (currentExtensionId == extension.getId())
        {
            resetCurrentExtension();
        }
    }

    /**
     * Resets the current building extension if the worker indicates this building extension should no longer be worked on.
     */
    public void resetCurrentExtension()
    {
        if (currentExtensionId != null)
        {
            checkedExtensions.put(currentExtensionId, building.getColony().getWorld().getGameTime());
        }
        currentExtensionId = null;
    }
}
