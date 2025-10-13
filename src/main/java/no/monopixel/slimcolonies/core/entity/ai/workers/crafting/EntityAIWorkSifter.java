package no.monopixel.slimcolonies.core.entity.ai.workers.crafting;

import no.monopixel.slimcolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.crafting.ItemStorage;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.AITarget;
import no.monopixel.slimcolonies.api.entity.ai.statemachine.states.IAIState;
import no.monopixel.slimcolonies.api.items.ModTags;
import no.monopixel.slimcolonies.api.util.InventoryUtils;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.SoundUtils;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.BuildingSifter;
import no.monopixel.slimcolonies.core.colony.interactionhandling.StandardInteraction;
import no.monopixel.slimcolonies.core.colony.jobs.JobSifter;
import no.monopixel.slimcolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import no.monopixel.slimcolonies.core.util.WorkerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static no.monopixel.slimcolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.SIFTER_NO_MESH;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;

/**
 * Sifter AI class.
 */
public class EntityAIWorkSifter extends AbstractEntityAICrafting<JobSifter, BuildingSifter>
{
    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 10;

    /**
     * Chance for the sifter to dump his inventory.
     */
    private static final int CHANCE_TO_DUMP_INV = 10;

    /**
     * The delay before processing again when there is no mesh in the building
     */
    private static final int NO_MESH_DELAY = 100;

    /**
     * Progress of hitting the block.
     */
    protected int progress = 0;

    /**
     * Constructor for the sifter. Defines the tasks the cook executes.
     *
     * @param job a sifter job to use.
     */
    public EntityAIWorkSifter(@NotNull final JobSifter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(SIFT, this::sift, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingSifter> getExpectedBuildingClass()
    {
        return BuildingSifter.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    @Override
    protected IAIState decide()
    {
        return SIFT;
    }

    @Override
    public boolean hasWorkToDo()
    {
        return super.hasWorkToDo() || building.getCurrentDailyQuantity() < building.getMaxDailyQuantity();
    }

    /**
     * The sifting process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState sift()
    {
        // Go idle if we can't do any more today
        if (building.getCurrentDailyQuantity() >= building.getMaxDailyQuantity())
        {
            return IDLE;
        }

        if (!walkToBuilding())
        {
            return getState();
        }

        if (!worker.getInventoryCitizen().hasSpace())
        {
            return INVENTORY_FULL;
        }

        if (currentRecipeStorage == null)
        {
            final ICraftingBuildingModule module = building.getFirstModuleOccurance(BuildingSifter.CraftingModule.class);
            currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);
        }

        if (currentRecipeStorage == null)
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, i -> i.is(ModTags.meshes), 1) == 0)
            {
                if (InventoryUtils.getItemCountInProvider(worker, i -> i.is(ModTags.meshes)) > 0)
                {
                    // We don't want the mesh in our inventory, we 'craft' out of the building
                    incrementActionsDone();
                    return INVENTORY_FULL;
                }
                if (worker.getCitizenData() != null)
                {
                    worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(SIFTER_NO_MESH), ChatPriority.IMPORTANT));
                    setDelay(NO_MESH_DELAY);
                }
            }
            if (!ItemStackUtils.isEmpty(worker.getMainHandItem()))
            {
                worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            if (!ItemStackUtils.isEmpty(worker.getOffhandItem()))
            {
                worker.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            progress = 0;
            return START_WORKING;
        }

        final ItemStack meshItem = currentRecipeStorage.getCraftingTools().get(0);
        final ItemStack inputItem = currentRecipeStorage.getCleanedInput().stream()
                                      .map(ItemStorage::getItemStack)
                                      .filter(item -> !ItemStackUtils.compareItemStacksIgnoreStackSize(item, meshItem, false, true))
                                      .findFirst().orElse(ItemStack.EMPTY);

        if (meshItem.isEmpty() || inputItem.isEmpty())
        {
            currentRecipeStorage = null;
            return getState();
        }

        if (!inputItem.isEmpty() && (ItemStackUtils.isEmpty(worker.getMainHandItem()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getMainHandItem(), inputItem)))
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, inputItem);
        }
        if (!meshItem.isEmpty() && (ItemStackUtils.isEmpty(worker.getOffhandItem()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getOffhandItem(),
          meshItem,
          false,
          true)))
        {
            worker.setItemInHand(InteractionHand.OFF_HAND, meshItem);
        }

        WorkerUtil.faceBlock(building.getPosition(), worker);

        progress++;

        if (progress > MAX_LEVEL - (getEffectiveSkillLevel(getSecondarySkillLevel()) / 2))
        {
            progress = 0;
            building.setCurrentDailyQuantity(building.getCurrentDailyQuantity() + 1);
            if (building.getCurrentDailyQuantity() >= building.getMaxDailyQuantity() || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < CHANCE_TO_DUMP_INV)
            {
                incrementActionsDoneAndDecSaturation();
            }

            StatsUtil.trackStatByName(building, ITEM_USED, inputItem.getHoverName(), inputItem.getCount());
            List<ItemStack> outputs = currentRecipeStorage.fullfillRecipeAndCopy(getLootContext(), building.getHandlers(), true);
            if (outputs == null)
            {
                currentRecipeStorage = null;
                return getState();
            }

            for (ItemStack stackForStats : outputs)
            {
                StatsUtil.trackStatByName(building, ITEM_OBTAINED, stackForStats.getHoverName(), stackForStats.getCount());
            }

            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(meshItem, building.getID()), worker);
        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, building.getID().below()), worker);

        worker.swing(InteractionHand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, building.getID(), SoundEvents.LEASH_KNOT_BREAK);
        return getState();
    }
}
