package no.monopixel.slimcolonies.core.colony.jobs;

import no.monopixel.slimcolonies.api.client.render.modeltype.ModModelTypes;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.core.colony.buildings.modules.ExpeditionLogModule;
import no.monopixel.slimcolonies.core.colony.buildings.modules.expedition.ExpeditionLog;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingNetherWorker;
import no.monopixel.slimcolonies.core.entity.ai.workers.production.EntityAIWorkNether;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FIRE_DAMAGE_PREDICATE;
import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.FIRE_RES;

public class JobNetherWorker extends AbstractJobCrafter<EntityAIWorkNether, JobNetherWorker>
{
    /**
     * Is the worker in the nether?
     */
    private boolean citizenInNether = false;

    /**
     * Queue of items produced from the initial crafting, containing tokens to be processed
     */
    private Queue<ItemStack> craftedResults =new LinkedList<>();

    /**
     * Post processed queue, no longer contains tokens, or items that were unable to be 'mined' due to tool breakage
     */
    private Queue<ItemStack> processedResults = new LinkedList<>();

    /**
     * Flag indicating worker is returning from nether and needs to process results.
     * Set when worker enters portal, cleared after simulation completes.
     */
    private boolean workerReturningFromNether = false;

    /**
     * Flag indicating whether the last trip was a retreat (true) or completed (false).
     * Used to set final expedition log status when worker returns.
     */
    private boolean lastTripWasRetreat = false;

    /**
     * Tag for storage of the citizenInNether value
     */
    private final String TAG_IN_NETHER = "inNether";

    /**
     * Tag for storage of the workerReturningFromNether flag
     */
    private final String TAG_RETURNING_FROM_NETHER = "returningFromNether";

    /**
     * Tag for storage of the lastTripWasRetreat flag
     */
    private final String TAG_LAST_TRIP_RETREAT = "lastTripRetreat";

    /**
     * Tag for storage of the craftedResults queue
     */
    private final String TAG_CRAFTED = "craftedResults";

    /**
     * Tag for storage of the processedResults queue
     */
    private final String TAG_PROCESSED = "processedResults";

    public JobNetherWorker(ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        @NotNull final ListTag craftedList = new ListTag();
        craftedResults.forEach(item -> {
            @NotNull final CompoundTag itemCompound = item.serializeNBT();
            craftedList.add(itemCompound);
        });
        compound.put(TAG_CRAFTED, craftedList);

        @NotNull final ListTag processedList = new ListTag();
        processedResults.forEach(item -> {
            @NotNull final CompoundTag itemCompound = item.serializeNBT();
            processedList.add(itemCompound);
        });
        compound.put(TAG_PROCESSED, processedList);

        compound.putBoolean(TAG_IN_NETHER, citizenInNether);
        compound.putBoolean(TAG_RETURNING_FROM_NETHER, workerReturningFromNether);
        compound.putBoolean(TAG_LAST_TRIP_RETREAT, lastTripWasRetreat);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        final ListTag craftedList = compound.getList(TAG_CRAFTED, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < craftedList.size(); ++i)
        {
            final CompoundTag itemCompound = craftedList.getCompound(i);
            craftedResults.add(ItemStack.of(itemCompound));
        }

        final ListTag processedList = compound.getList(TAG_PROCESSED, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < processedList.size(); ++i)
        {
            final CompoundTag itemCompound = processedList.getCompound(i);
            processedResults.add(ItemStack.of(itemCompound));
        }


        if (compound.contains(TAG_IN_NETHER))
        {
            citizenInNether = compound.getBoolean(TAG_IN_NETHER);
        }

        if (compound.contains(TAG_RETURNING_FROM_NETHER))
        {
            workerReturningFromNether = compound.getBoolean(TAG_RETURNING_FROM_NETHER);
        }

        if (compound.contains(TAG_LAST_TRIP_RETREAT))
        {
            lastTripWasRetreat = compound.getBoolean(TAG_LAST_TRIP_RETREAT);
        }
    }

    @Override
    public EntityAIWorkNether generateAI()
    {
        return new EntityAIWorkNether(this);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.NETHERWORKER_ID;
    }


    @Override
    public int getIdleSeverity(boolean isDemand)
    {
        if(isDemand)
        {
            return super.getIdleSeverity(isDemand);
        }
        else
        {
            // Shorten the time for asking for materials.
            return 4;
        }
    }

    /**
     * Mark the worker as in the nether or not.
     * @param away true if in the nether
     */
    public void setInNether(boolean away)
    {
        citizenInNether = away;
    }

    /**
     * Check if the citizen is in the nether currently
     */
    public boolean isInNether()
    {
        return citizenInNether;
    }

    /**
     * Check if worker is returning from nether.
     */
    public boolean isWorkerReturningFromNether()
    {
        return workerReturningFromNether;
    }

    /**
     * Set flag indicating worker is returning from nether.
     */
    public void setWorkerReturningFromNether(boolean returning)
    {
        workerReturningFromNether = returning;
    }

    /**
     * Check if last trip was a retreat.
     */
    public boolean wasLastTripRetreat()
    {
        return lastTripWasRetreat;
    }

    /**
     * Set flag indicating whether last trip was a retreat.
     */
    public void setLastTripRetreat(boolean wasRetreat)
    {
        lastTripWasRetreat = wasRetreat;
    }

    /**
     * Get the queue of CraftedResults
     * This queue is not immutable and OK to modify
     */
    public Queue<ItemStack> getCraftedResults()
    {
        return craftedResults;
    }

    /**
     * Add a list of items to the crafted results list
     * @param newResults items to add
     * @return true if success
     */
    public boolean addCraftedResultsList(Collection<ItemStack> newResults)
    {
        return craftedResults.addAll(newResults);
    }

    /**
     * Get the queue of ProcessedResults
     * This queue is not immutable and OK to modify
     */
    public Queue<ItemStack> getProcessedResults()
    {
        return processedResults;
    }

    /**
     * Add a list of items to the processed results list
     * @param newResults items to add
     * @return true if success
     */
    public boolean addProcessedResultsList(Collection<ItemStack> newResults)
    {
        return processedResults.addAll(newResults);
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource.typeHolder().is(FIRE_DAMAGE_PREDICATE))
        {
            return getColony().getResearchManager().getResearchEffects().getEffectStrength(FIRE_RES) > 0;
        }

        return super.ignoresDamage(damageSource);
    }

    @Override
    public void onWakeUp()
    {
        super.onWakeUp();

        // If worker just returned from nether, update expedition status immediately
        if (workerReturningFromNether && getCitizen().getWorkBuilding() instanceof BuildingNetherWorker building)
        {
            final ExpeditionLog expeditionLog = building.getFirstModuleOccurance(ExpeditionLogModule.class).getLog();
            if (lastTripWasRetreat)
            {
                expeditionLog.setStatus(ExpeditionLog.Status.RETREATED);
            }
            else
            {
                expeditionLog.setStatus(ExpeditionLog.Status.COMPLETED);
            }
        }
    }
}
