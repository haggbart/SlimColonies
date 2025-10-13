package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import com.google.common.collect.ImmutableList;
import no.monopixel.slimcolonies.api.colony.requestsystem.request.RequestState;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.entity.citizen.VisibleCitizenStatus;
import no.monopixel.slimcolonies.api.util.SoundUtils;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingCrusher;
import no.monopixel.slimcolonies.core.colony.jobs.JobCrusher;
import no.monopixel.slimcolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import no.monopixel.slimcolonies.core.util.WorkerUtil;
import no.monopixel.slimcolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Crusher AI class.
 */
public class EntityAIWorkCrusher extends AbstractEntityAICrafting<JobCrusher, BuildingCrusher>
{
    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 40;

    /**
     * Crusher icon
     */
    private final static VisibleCitizenStatus CRUSHING =
        new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/crusher.png"), "no.monopixel.slimcolonies.gui.visiblestatus.crusher");

    /**
     * Constructor for the crusher. Defines the tasks the crusher executes.
     *
     * @param job a crusher job to use.
     */
    public EntityAIWorkCrusher(@NotNull final JobCrusher job)
    {
        super(job);
        super.registerTargets(
            new AITarget(IDLE, START_WORKING, 1),
            new AITarget(CRUSH, this::crush, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingCrusher> getExpectedBuildingClass()
    {
        return BuildingCrusher.class;
    }

    @Override
    protected IAIState decide()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        if (job.getTaskQueue().isEmpty() || job.getCurrentTask() == null)
        {
            if (building.getCurrentDailyQuantity() < building.getSetting(BuildingCrusher.DAILY_LIMIT).getValue())
            {
                return CRUSH;
            }

            return IDLE;
        }

        if (!walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() >= getActionsDoneUntilDumping())
        {
            // Wait to dump before continuing.
            return getState();
        }

        return getNextCraftingState();
    }

    @Override
    public boolean hasWorkToDo()
    {
        return super.hasWorkToDo() || building.getCurrentDailyQuantity() < building.getSetting(BuildingCrusher.DAILY_LIMIT).getValue();
    }

    /**
     * The crushing process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState crush()
    {
        if (!walkToBuilding())
        {
            return getState();
        }

        worker.getCitizenData().setVisibleStatus(CRUSHING);
        job.setProgress(job.getProgress() + TICK_DELAY);

        final BuildingCrusher crusherBuilding = building;
        WorkerUtil.faceBlock(crusherBuilding.getPosition(), worker);

        final IRecipeStorage recipeMode = crusherBuilding.getSetting(BuildingCrusher.MODE).getValue(crusherBuilding);
        final int dailyLimit = crusherBuilding.getSetting(BuildingCrusher.DAILY_LIMIT).getValue();
        if (currentRecipeStorage == null)
        {
            currentRecipeStorage = recipeMode;
        }

        if ((getState() != CRAFT && crusherBuilding.getCurrentDailyQuantity() >= dailyLimit) || currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        final IAIState check = checkForItems(currentRecipeStorage);
        if (job.getProgress() > MAX_LEVEL - Math.min((getSecondarySkillLevel() / 2) + 1, MAX_LEVEL))
        {
            job.setProgress(0);

            if (check == CRAFT)
            {
                if (getState() != CRAFT)
                {
                    crusherBuilding.setCurrentDailyQuantity(crusherBuilding.getCurrentDailyQuantity() + 1);
                    if (crusherBuilding.getCurrentDailyQuantity() >= dailyLimit)
                    {
                        incrementActionsDoneAndDecSaturation();
                    }
                }
                if (currentRequest != null)
                {
                    currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                }

                worker.swing(InteractionHand.MAIN_HAND);
                job.setCraftCounter(job.getCraftCounter() + 1);
                currentRecipeStorage.fullfillRecipe(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen()));

                worker.getCitizenExperienceHandler().addExperience(0.1);
                recordCraftingBuildingStats(currentRequest, currentRecipeStorage);
            }
            else if (getState() != CRAFT)
            {
                currentRecipeStorage = recipeMode;
                final int requestQty = Math.min((dailyLimit - crusherBuilding.getCurrentDailyQuantity()) * 2, STACKSIZE);
                if (requestQty <= 0)
                {
                    return START_WORKING;
                }
                final ItemStack stack = currentRecipeStorage.getInput().get(0).getItemStack().copy();
                stack.setCount(requestQty);
                checkIfRequestForItemExistOrCreateAsync(stack);
                return START_WORKING;
            }
            else
            {
                return check;
            }
        }
        if (check == CRAFT)
        {
            Network.getNetwork()
                .sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getInput().get(0).getItemStack().copy(), crusherBuilding.getID()), worker);
            Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getPrimaryOutput().copy(), crusherBuilding.getID().below()),
                worker);
            SoundUtils.playSoundAtCitizen(world, building.getID(), SoundEvents.STONE_BREAK);
        }
        return getState();
    }

    /**
     * The actual crafting logic.
     *
     * @return the next state to go to.
     */
    @Override
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (currentRequest == null && job.getCurrentTask() != null)
        {
            return GET_RECIPE;
        }

        if (!walkToBuilding())
        {
            return getState();
        }

        job.setProgress(job.getProgress() + 1);

        worker.setItemInHand(InteractionHand.MAIN_HAND,
            currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        worker.setItemInHand(InteractionHand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        CitizenItemUtils.hitBlockWithToolInHand(worker, building.getPosition());

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        final IAIState check = crush();
        if (check == getState())
        {
            if (job.getCraftCounter() >= job.getMaxCraftingCount())
            {
                incrementActionsDone(getActionRewardForCraftingSuccess());
                currentRecipeStorage = null;
                resetValues();

                if (inventoryNeedsDump())
                {
                    if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
                    {
                        job.finishRequest(true);
                    }
                }
            }
        }
        else
        {
            currentRequest = null;
            job.finishRequest(false);
            incrementActionsDoneAndDecSaturation();
            resetValues();
        }

        return getState();
    }
}
