package no.monopixel.slimcolonies.core.colony.jobs;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.requestsystem.StandardFactoryController;
import no.monopixel.slimcolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.IRequest;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestState;
import no.monopixel.slimcolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.util.constant.NbtTagConstants;
import no.monopixel.slimcolonies.api.util.constant.TypeConstants;
import no.monopixel.slimcolonies.core.colony.buildings.AbstractBuilding;
import no.monopixel.slimcolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;


/**
 * Class of the crafter job.
 */
public abstract class AbstractJobCrafter<AI extends AbstractEntityAIBasic<J, ? extends AbstractBuilding>, J extends AbstractJobCrafter<AI, J>>
  extends AbstractJob<AI, J>
{
    /**
     * The Token of the data store which belongs to this job.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * Max crafting count for current recipe.
     */
    private int maxCraftingCount = 0;

    /**
     * Count of already executed recipes.
     */
    private int craftCounter = 0;

    /**
     * Progress of hitting the block.
     */
    private int progress = 0;

    /**
     * Instantiates the job for the crafter.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public AbstractJobCrafter(final ICitizenData entity)
    {
        super(entity);
        if (entity != null)
        {
            setupRsDataStore();
        }
    }

    /**
     * Data store setup.
     */
    private void setupRsDataStore()
    {
        rsDataStoreToken = this.getCitizen()
                             .getColony()
                             .getRequestManager()
                             .getDataStoreManager()
                             .get(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                               TypeConstants.REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE)
                             .getId();
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.CRAFTER_ID;
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buffer)
    {
        super.serializeToView(buffer);
        StandardFactoryController.getInstance().serialize(buffer, rsDataStoreToken);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.put(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
        compound.putInt(NbtTagConstants.TAG_PROGRESS, progress);
        compound.putInt(NbtTagConstants.TAG_MAX_COUNTER, maxCraftingCount);
        compound.putInt(NbtTagConstants.TAG_CRAFT_COUNTER, craftCounter);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.contains(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance()
                                 .deserialize(compound.getCompound(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }

        if (compound.contains(NbtTagConstants.TAG_PROGRESS))
        {
            this.progress = compound.getInt(NbtTagConstants.TAG_PROGRESS);
        }

        if (compound.contains(NbtTagConstants.TAG_MAX_COUNTER))
        {
            this.progress = compound.getInt(NbtTagConstants.TAG_MAX_COUNTER);
        }

        if (compound.contains(NbtTagConstants.TAG_CRAFT_COUNTER))
        {
            this.progress = compound.getInt(NbtTagConstants.TAG_CRAFT_COUNTER);
        }
    }

    /**
     * Getter for the data store which belongs to this job.
     *
     * @return the crafter data store.
     */
    private IRequestSystemCrafterJobDataStore getDataStore()
    {
        return getCitizen().getColony()
                 .getRequestManager()
                 .getDataStoreManager()
                 .get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE);
    }

    /**
     * Retrieve the task queue from the data store.
     *
     * @return the linked queue.
     */
    private LinkedList<IToken<?>> getTaskQueueFromDataStore()
    {
        return getDataStore().getQueue();
    }

    public List<IToken<?>> getAssignedTasksFromDataStore()
    {
        return getDataStore().getAssignedTasks();
    }


    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @param <R> the request type.
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings("unchecked")
    public <R extends PublicCrafting> IRequest<R> getCurrentTask()
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return null;
        }

        // This cleans up the state after something went wrong.
        IRequest<R> request = (IRequest<R>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
        while (request == null)
        {
            getTaskQueueFromDataStore().remove(getTaskQueueFromDataStore().peekFirst());
            request = (IRequest<R>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
        }

        return request;
    }

    /**
     * Method used to add a request to the queue
     *
     * @param token The token of the requests to add.
     */
    public void addRequest(@NotNull final IToken<?> token)
    {
        getTaskQueueFromDataStore().add(token);
    }

    /**
     * Method called to mark the current request as finished.
     *
     * @param successful True when the processing was successful, false when not.
     */
    public void finishRequest(final boolean successful)
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return;
        }

        final IToken<?> current = getTaskQueueFromDataStore().getFirst();

        getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.FAILED);
    }

    /**
     * Called when a task that is being scheduled is being canceled.
     *
     * @param token token of the task to be deleted.
     */
    public void onTaskDeletion(@NotNull final IToken<?> token)
    {
        if (getTaskQueueFromDataStore().contains(token))
        {
            getTaskQueueFromDataStore().remove(token);
        }
        else if (getAssignedTasksFromDataStore().contains(token))
        {
            getAssignedTasksFromDataStore().remove(token);
        }
    }

    public void onTaskBeingScheduled(@NotNull final IToken<?> token)
    {
        getAssignedTasksFromDataStore().add(token);
    }

    public void onTaskBeingResolved(@NotNull final IToken<?> token)
    {
        onTaskDeletion(token);
        addRequest(token);
    }

    /**
     * Method to get the task queue of this job.
     *
     * @return The task queue.
     */
    public List<IToken<?>> getTaskQueue()
    {
        return ImmutableList.copyOf(getTaskQueueFromDataStore());
    }

    public List<IToken<?>> getAssignedTasks()
    {
        return ImmutableList.copyOf(getAssignedTasksFromDataStore());
    }

    /**
     * Get the max crafting count for the current recipe.
     *
     * @return the count.
     */
    public int getMaxCraftingCount()
    {
        return maxCraftingCount;
    }

    /**
     * Set the max crafting count for the current recipe.
     *
     * @param maxCraftingCount the count to set.
     */
    public void setMaxCraftingCount(final int maxCraftingCount)
    {
        this.maxCraftingCount = maxCraftingCount;
    }

    /**
     * Get the current craft counter.
     *
     * @return the counter.
     */
    public int getCraftCounter()
    {
        return craftCounter;
    }

    /**
     * Set the current craft counter.
     *
     * @param craftCounter the counter to set.
     */
    public void setCraftCounter(final int craftCounter)
    {
        this.craftCounter = craftCounter;
    }

    /**
     * Get the crafting progress.
     *
     * @return the current progress.
     */
    public int getProgress()
    {
        return progress;
    }

    /**
     * Set the crafting progress.
     *
     * @param progress the current progress.
     */
    public void setProgress(final int progress)
    {
        this.progress = progress;
    }

    @Override
    public void onRemoval()
    {
        super.onRemoval();
        cancelAssignedRequests();
        getColony().getRequestManager().getDataStoreManager().remove(this.rsDataStoreToken);
    }

    private void cancelAssignedRequests()
    {
        for (final IToken<?> t : getTaskQueue())
        {
            getColony().getRequestManager().updateRequestState(t, RequestState.FAILED);
        }
    }

    /**
     * Play a job specific work sound at a pos.
     * @param blockPos the pos to play it at.
     * @param worker the worker to play it for.
     */
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        // Child override if necessary
    }
}
